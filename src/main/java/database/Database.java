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
import java.util.List;
import java.util.Properties;
import java.util.Set;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Database {
    private static final Logger log = LoggerFactory.getLogger(Database.class);
    private static final HikariConfig CONFIG = new HikariConfig();
    public static final int MAX_POOL_SIZE = 10;
    private static DataSource ds;

    private Database() {
    }

    public static DataSource getDataSource() {
        // lazy initialization
        if (ds != null) {
            return ds;
        }
        Properties props = readProperties("resources\\db.properties");

        CONFIG.setJdbcUrl(props.getProperty("url"));
        CONFIG.setUsername(props.getProperty("user"));
        CONFIG.setPassword(props.getProperty("password"));

        CONFIG.setMaximumPoolSize(MAX_POOL_SIZE);
        CONFIG.setAutoCommit(true);
        CONFIG.addDataSourceProperty("cachePrepStmts", "true");
        CONFIG.addDataSourceProperty("prepStmtCacheSize", "250");
        CONFIG.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        ds = new HikariDataSource(CONFIG);
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

            st.executeUpdate("CREATE TABLE IF NOT EXISTS chain("
                    + " id BIGINT,"
                    + " name VARCHAR(30),"
                    + " PRIMARY KEY (id)"
                    + ")");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS store("
                    + " id INTEGER,"
                    + " chain_id BIGINT REFERENCES chain(id),"
                    + " name VARCHAR(50),"
                    + " address VARCHAR(50),"
                    + " city VARCHAR(20),"
                    + " type INT,"
                    + " PRIMARY KEY (id, chain_id)"
                    + ")");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS item("
                    + " id BIGINT,"
                    + " name VARCHAR(50),"
                    + " manufacturer_name VARCHAR(50),"
                    + " manufacture_country VARCHAR(20),"
                    + " unit_quantity VARCHAR(10),"
                    + " quantity REAL,"
                    + " measure_unit VARCHAR(10),"
                    + " measure_price REAL,"
                    + " PRIMARY KEY (id)"
                    + ")");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS price("
                    + " item_id BIGINT REFERENCES item(id),"
                    + " chain_id BIGINT,"
                    + " store_id INTEGER,"
                    + " price REAL,"
                    + " PRIMARY KEY (item_id, chain_id, store_id),"
                    + " FOREIGN KEY (store_id, chain_id) REFERENCES store (id, chain_id)"
                    + ")");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS unknown_measure("
                    + " name VARCHAR(20),"
                    + " PRIMARY KEY (name)"
                    + ")");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS parsed_files("
                    + " name VARCHAR(50),"
                    + " PRIMARY KEY (name)"
                    + ")");

            st.executeUpdate("DROP TRIGGER IF EXISTS z_min_update ON item;"
                    + " CREATE TRIGGER z_min_update"
                    + " BEFORE UPDATE ON item"
                    + " FOR EACH ROW EXECUTE PROCEDURE suppress_redundant_updates_trigger();");
            st.executeUpdate("DROP TRIGGER IF EXISTS z_min_update ON price;"
                    + " CREATE TRIGGER z_min_update"
                    + " BEFORE UPDATE ON price"
                    + " FOR EACH ROW EXECUTE PROCEDURE suppress_redundant_updates_trigger();");
            st.executeUpdate("DROP TRIGGER IF EXISTS z_min_update ON store;"
                    + " CREATE TRIGGER z_min_update"
                    + " BEFORE UPDATE ON store"
                    + " FOR EACH ROW EXECUTE PROCEDURE suppress_redundant_updates_trigger();");
            st.executeUpdate("DROP TRIGGER IF EXISTS z_min_update ON chain;"
                    + " CREATE TRIGGER z_min_update"
                    + " BEFORE UPDATE ON chain"
                    + " FOR EACH ROW EXECUTE PROCEDURE suppress_redundant_updates_trigger();");
            st.executeUpdate("DROP TRIGGER IF EXISTS z_min_update ON parsed_files;"
                    + " CREATE TRIGGER z_min_update"
                    + " BEFORE UPDATE ON parsed_files"
                    + " FOR EACH ROW EXECUTE PROCEDURE suppress_redundant_updates_trigger();");
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
