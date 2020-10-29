/*
 *
 *  * Copyright 2020 Imri
 *  *
 *  * This application is free software; you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package database;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.entities.Item;

public final class Database {
    private static final Logger log = LoggerFactory.getLogger(Database.class);

    private static DataSource ds;

    private Database() {
    }

    public static DataSource getDataSource() {
        // lazy initialization
        if (ds != null) {
            return ds;
        }
        Properties props = readProperties("db.properties");
        HikariConfig config = new HikariConfig(props);
        ds = new HikariDataSource(config);
        return ds;
    }

    public static void createTables(boolean override) throws SQLException {
        try (Connection db = getDataSource().getConnection();
             Statement st = db.createStatement()) {
            if (override) {
                st.executeUpdate("DROP TABLE IF EXISTS price");
                st.executeUpdate("DROP TABLE IF EXISTS store");
                st.executeUpdate("DROP TABLE IF EXISTS chain");
                st.executeUpdate("DROP TABLE IF EXISTS item");
                st.executeUpdate("DROP TABLE IF EXISTS parsed_files");
                st.executeUpdate("DROP TABLE IF EXISTS unknown_measure");
            }

            final String createChain = "CREATE TABLE IF NOT EXISTS chain("
                    + " id BIGINT,"
                    + " name VARCHAR(30),"
                    + " PRIMARY KEY (id)"
                    + ")";
            final String createStore = "CREATE TABLE IF NOT EXISTS store("
                    + " id INTEGER,"
                    + " chain_id BIGINT REFERENCES chain(id),"
                    + " name VARCHAR(50),"
                    + " address VARCHAR(50),"
                    + " city VARCHAR(20),"
                    + " type INT,"
                    + " PRIMARY KEY (id, chain_id)"
                    + ")";
            final String unitQuantityStr = Arrays.stream(Item.QuantityUnit.values())
                                                 .map(Objects::toString)
                                                 .collect(Collectors.joining("','", "'", "'"));
            final String createItem = "CREATE TABLE IF NOT EXISTS item("
                    + " id BIGINT,"
                    + " name VARCHAR(50),"
                    + " manufacturer_name VARCHAR(50),"
                    + " manufacture_country VARCHAR(20),"
                    + " unit_quantity VARCHAR(10) CHECK (unit_quantity IN (" + unitQuantityStr + ")),"
                    + " quantity REAL,"
                    + " measure_unit VARCHAR(10),"
                    + " PRIMARY KEY (id)"
                    + ")";
            final String createPrice = "CREATE TABLE IF NOT EXISTS price("
                    + " item_id BIGINT REFERENCES item(id),"
                    + " chain_id BIGINT,"
                    + " store_id INTEGER,"
                    + " price REAL,"
                    + " PRIMARY KEY (item_id, chain_id, store_id),"
                    + " FOREIGN KEY (store_id, chain_id) REFERENCES store (id, chain_id)"
                    + ")";
            final String createUnknownMeasure = "CREATE TABLE IF NOT EXISTS unknown_measure("
                    + " name VARCHAR(20),"
                    + " PRIMARY KEY (name)"
                    + ")";
            final String createParsedFiles = "CREATE TABLE IF NOT EXISTS parsed_files("
                    + " name VARCHAR(50),"
                    + " PRIMARY KEY (name)"
                    + ")";

            final ArrayList<String> tableCreateList = new ArrayList<>(
                    List.of(createChain, createStore, createItem, createPrice, createUnknownMeasure, createParsedFiles));
            for (String createSql : tableCreateList) {
                try {
                    st.execute(createSql);
                } catch (SQLException e) {
                    throw new SQLException("Error in query: " + createSql, e);
                }
            }

            // Add the z_min_update trigger to these tables
            final List<String> triggerList = new ArrayList<>(List.of("item", "price", "store", "chain", "parsed_files"));
            final String triggerTemplate = "DROP TRIGGER IF EXISTS z_min_update ON %s;"
                    + " CREATE TRIGGER z_min_update"
                    + " BEFORE UPDATE ON %s"
                    + " FOR EACH ROW EXECUTE PROCEDURE suppress_redundant_updates_trigger();";

            for (String tableName : triggerList) {
                st.execute(String.format(triggerTemplate, tableName, tableName));
            }
        }
    }

    public static void clearTables() {
        try (Connection db = getDataSource().getConnection();
             Statement st = db.createStatement()) {
            st.executeUpdate("TRUNCATE chain, store, item, price, unknown_measure, parsed_files CASCADE;");
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private static Properties readProperties(String uri) {
        Path path = Paths.get(uri);
        Properties props = new Properties();

        try {
            BufferedReader bf = Files.newBufferedReader(path,
                    StandardCharsets.UTF_8);

            props.load(bf);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return props;
    }

    public static void updateMeasures(Set<String> measures) {
        String sql = "INSERT INTO unknown_measure (name) VALUES (?) ON CONFLICT (name) DO NOTHING;";
        try (Connection db = ds.getConnection()) {
            try (PreparedStatement pstItem = db.prepareStatement(sql)) {
                db.setAutoCommit(false);
                final int batchSize = 1000;
                int count = 0;
                for (String m : measures) {
                    pstItem.setString(1, m);
                    pstItem.addBatch();

                    if (++count % batchSize == 0) {
                        pstItem.executeBatch();
                    }
                }
                pstItem.executeBatch();
                db.commit();
            } catch (SQLException e) {
                db.rollback();
                e.printStackTrace();
                log.error("", e);
            } finally {
                db.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("", e);
        }
    }

    public static void addParsedFile(String name) {
        String sql = "INSERT INTO parsed_files (name) VALUES (?) ON CONFLICT DO NOTHING";
        try (Connection db = getDataSource().getConnection();
             PreparedStatement pst = db.prepareStatement(sql)) {
            pst.setString(1, name);
            pst.executeUpdate();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static List<String> getParsedFiles() {
        List<String> files = new ArrayList<>();
        String query = "SELECT name FROM parsed_files";

        try (Connection db = ds.getConnection();
             PreparedStatement pst = db.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                files.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("", e);
        }

        return files;
    }
}
