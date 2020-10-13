package database;

import java.util.List;

import server.entities.Item;

public interface ItemDao {
    List<Item> searchItems(String name);

    List<Item> getAllItems();

    List<Item> getIncompleteItems();

    Item getItem(long id);

    void addItems(List<Item> items);

    void updateItem(Item item);
}
