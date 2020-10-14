package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.entities.Item;
import server.entities.StoreItems;

public class StoreItemsDaoPostgres implements StoreItemsDao {
    private static final Logger log = LoggerFactory.getLogger(StoreItemsDaoPostgres.class);
    private final DataSource ds;

    public StoreItemsDaoPostgres(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public StoreItems getStoreItems(long chainId, int storeId) {
        StoreItems storeItems = new StoreItems();

        try (Connection db = ds.getConnection();
             PreparedStatement pst = db.prepareStatement(GET_STORE_ITEMS_SQL)) {

            pst.setLong(1, chainId);
            pst.setLong(2, storeId);
            try (ResultSet rs = pst.executeQuery()) {
                while (rs.next()) {
                    storeItems.addItem(extractItemFromResult(rs));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("", e);
        }

        return storeItems;
    }

    @Override
    public void addStoreItems(StoreItems storeItems) {
        try (Connection db = ds.getConnection()) {
            try (PreparedStatement pstPrice = db.prepareStatement(ADD_PRICE_SQL)) {
                db.setAutoCommit(false);
                final int batchSize = 1000;
                int count = 0;
                for (Item item : storeItems.getItems()) {
                    pstPrice.setLong(1, item.getId());
                    pstPrice.setLong(2, storeItems.getChainId());
                    pstPrice.setInt(3, storeItems.getStoreId());
                    pstPrice.setFloat(4, item.getPrice());
                    pstPrice.addBatch();

                    if (++count % batchSize == 0) {
                        pstPrice.executeBatch();
                    }
                }
                pstPrice.executeBatch();
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

    private Item extractItemFromResult(ResultSet rs) throws SQLException {
        return new Item(rs.getString("name"),
                rs.getString("manufacturer_name"),
                rs.getString("manufacture_country"),
                rs.getString("unit_quantity"),
                rs.getFloat("quantity"),
                rs.getString("measure_unit"),
                rs.getFloat("price"),
                rs.getLong("id"));
    }

    private static final String ADD_PRICE_SQL =
            "INSERT INTO price (item_id, chain_id, store_id, price) VALUES (?, ?, ?, ?)" +
                    "ON CONFLICT (item_id, chain_id, store_id) " +
                    "DO UPDATE " +
                    "SET price = EXCLUDED.price;";
    private static final String GET_STORE_ITEMS_SQL =
            "SELECT * FROM price " +
                    "JOIN item ON price.item_id = item.id " +
                    "WHERE chain_id = ? AND store_id = ?;";
}
