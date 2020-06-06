package database;

import server.Item;
import server.StoreItems;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class StoreItemsDaoPostgres implements StoreItemsDao {

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
            System.out.println(e.getMessage());
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
                System.out.println(e.getMessage());
            } finally {
                db.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println(e.getMessage());
        }
    }

    private Item extractItemFromResult(ResultSet rs) throws SQLException {
        return new Item(rs.getString("name"),
                rs.getString("manufacturer_name"),
                rs.getString("manufacture_country"),
                rs.getString("unit_quantity"),
                rs.getFloat("quantity"),
                rs.getString("measure_unit"),
                rs.getFloat("measure_price"),
                rs.getFloat("price"),
                rs.getLong("id"));
    }


    private static final String ADD_PRICE_SQL =
            "INSERT INTO price (item_id, chain_id, store_id, price) VALUES (?, ?, ?, ?)" +
                    "ON CONFLICT (item_id, chain_id, store_id) " +
                    "DO UPDATE " +
                    "SET price = EXCLUDED.price " +
                    "WHERE price <> EXCLUDED.price"; // TODO: test performance without condition
    private static final String GET_STORE_ITEMS_SQL =
            "SELECT * FROM price " +
                    "JOIN item ON price.item_id = item.id " +
                    "WHERE chain_id = ? AND store_id = ?";
}
