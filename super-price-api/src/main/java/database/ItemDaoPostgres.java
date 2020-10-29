package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.entities.Item;

public class ItemDaoPostgres implements ItemDao {
    private static final Logger log = LoggerFactory.getLogger(ItemDaoPostgres.class);
    private final DataSource ds;

    public ItemDaoPostgres(DataSource ds) {
        this.ds = ds;
    }

    @Override
    public ArrayList<Item> searchItems(String name) {
        assert false;
        return null;
    }

    @Override
    public ArrayList<Item> getAllItems() {
        return getItemsByQuery(GET_ALL_ITEMS_SQL);
    }

    @Override
    public ArrayList<Item> getIncompleteItems() {
        return getItemsByQuery(GET_INCOMPLETE_ITEMS_SQL);
    }

    private ArrayList<Item> getItemsByQuery(String query) {
        ArrayList<Item> items = new ArrayList<>();

        try (Connection db = ds.getConnection();
             PreparedStatement pst = db.prepareStatement(query);
             ResultSet rs = pst.executeQuery()) {
            while (rs.next()) {
                items.add(extractItemFromResult(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            log.error("", e);
        }

        return items;
    }

    private Item extractItemFromResult(ResultSet rs) throws SQLException {
        return new Item(rs.getString("name"),
                rs.getString("manufacturer_name"),
                rs.getString("manufacture_country"),
                rs.getString("unit_quantity"),
                rs.getFloat("quantity"),
                rs.getString("measure_unit"),
                -1, // Not needed
                rs.getLong("id"));
    }

    @Override
    public Item getItem(long id) {
        assert false;
        return null;
    }

    @Override
    public void addItems(List<Item> items) {
        try (Connection db = ds.getConnection()) {
            try (PreparedStatement pstItem = db.prepareStatement(ADD_ITEM_SQL)) {
                db.setAutoCommit(false);
                final int batchSize = 1000;
                int count = 0;
                for (Item item : items) {
                    pstItem.setLong(1, item.getId());
                    pstItem.setString(2, item.getName());
                    pstItem.setString(3, item.getManufacturerName());
                    pstItem.setString(4, item.getManufactureCountry());
                    pstItem.setString(5, item.getQuantityUnit().toString());
                    pstItem.setFloat(6, item.getQuantity());
                    pstItem.setString(7, item.getUnitOfMeasure());
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

    @Override
    public void updateItem(Item item) {
        assert false;
    }

    private static final String GET_ALL_ITEMS_SQL = "SELECT * FROM item";
    private static final String GET_INCOMPLETE_ITEMS_SQL =
            "SELECT * FROM item WHERE manufacturer_name IS NULL OR manufacture_country IS NULL " +
                    "OR unit_quantity = 'UNKNOWN' OR quantity = 0";
    private static final String ADD_ITEM_SQL =
            "INSERT INTO item (id, name, manufacturer_name, manufacture_country, unit_quantity, " +
                    "quantity, measure_unit) VALUES (?, ?, ?, ?, ?, ?, ?) " +
                    "ON CONFLICT (id) " +
                    "DO UPDATE " +
                    "SET name = COALESCE(EXCLUDED.name, item.name), " +
                    "manufacturer_name = COALESCE(EXCLUDED.manufacturer_name, item.manufacturer_name), " +
                    "manufacture_country = COALESCE(EXCLUDED.manufacture_country, item.manufacture_country), " +
                    "unit_quantity = COALESCE(EXCLUDED.unit_quantity, item.unit_quantity), " +
                    "quantity = COALESCE(EXCLUDED.quantity, NULLIF(item.quantity, 0)), " +
                    "measure_unit = COALESCE(EXCLUDED.measure_unit, item.measure_unit);";
}
