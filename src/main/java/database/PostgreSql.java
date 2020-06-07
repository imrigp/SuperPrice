package database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

public class PostgreSql {

    private static final HikariConfig config = new HikariConfig();
    private static DataSource ds;

    public static DataSource getDataSource() {
        if (ds != null) {
            return ds;
        }
        Properties props = readProperties("resources\\db.properties");

        config.setJdbcUrl(props.getProperty("url"));
        config.setUsername(props.getProperty("user"));
        config.setPassword(props.getProperty("password"));

        config.setMaximumPoolSize(10);
        config.setAutoCommit(true);
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        ds = new HikariDataSource(config);
        return ds;
    }

    public static void createTables() throws SQLException {
        try (Connection db = getDataSource().getConnection();
             Statement st = db.createStatement()) {

            st.executeUpdate("DROP TABLE IF EXISTS price");
            st.executeUpdate("DROP TABLE IF EXISTS store");
            st.executeUpdate("DROP TABLE IF EXISTS chain");
            st.executeUpdate("DROP TABLE IF EXISTS item");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS chain(" +
                    " id BIGINT," +
                    " name VARCHAR(30)," +
                    " PRIMARY KEY (id)" +
                    ")");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS store(" +
                    " id INTEGER," +
                    " chain_id BIGINT REFERENCES chain(id)," +
                    " name VARCHAR(50)," +
                    " address VARCHAR(50)," +
                    " city VARCHAR(20)," +
                    " type INT," +
                    " PRIMARY KEY (id)" +
                    ")");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS item(" +
                    " id BIGINT," +
                    " name VARCHAR(50)," +
                    " manufacturer_name VARCHAR(50)," +
                    " manufacture_country VARCHAR(20)," +
                    " unit_quantity VARCHAR(10)," +
                    " quantity REAL," +
                    " measure_unit VARCHAR(10)," +
                    " measure_price REAL," +
                    " PRIMARY KEY (id)" +
                    ")");

            st.executeUpdate("CREATE TABLE IF NOT EXISTS price(" +
                    " item_id BIGINT REFERENCES item(id)," +
                    " chain_id BIGINT REFERENCES chain(id), " +
                    " store_id INTEGER REFERENCES store(id)," +
                    " price REAL," +
                    " PRIMARY KEY (item_id, chain_id, store_id)" +
                    ")");
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
}
