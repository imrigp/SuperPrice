package database;

import java.util.List;
import java.util.Map;

import server.entities.Store;

public interface StoreDao {
    List<Store> getChainStores(long chainId);

    Store getStore(long chainId, int storeId);

    void addStore(long chainId, Store store);

    void addStores(long chainId, Map<Integer, Store> stores);
}
