package database;

import server.Item;

import java.util.ArrayList;

public interface ItemDao {
    ArrayList<Item> searchItems(String name);

    ArrayList<Item> getAllItems();

    ArrayList<Item> getIncompleteItems();

    Item getItem(long id);

    void addItems(ArrayList<Item> items);

    void updateItem(Item item);

}
