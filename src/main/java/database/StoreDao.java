package database;

import server.Store;

import java.util.ArrayList;

public interface StoreDao {
    ArrayList<Store> getChainStores(long chainId);

    Store getStore(long chainId, int storeId);

    void addStore(long chainId, Store store);

    void addStores(long chainId, ArrayList<Store> stores);
}
