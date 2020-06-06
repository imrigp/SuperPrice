package database;

import server.StoreItems;

public interface StoreItemsDao {
    StoreItems getStoreItems(long chainId, int storeId);

    void addStoreItems(StoreItems storeItems);
}
