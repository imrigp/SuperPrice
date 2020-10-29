package database;

import server.entities.StoreItems;

public interface StoreItemsDao {
    StoreItems getStoreItems(long chainId, int storeId);

    void addStoreItems(StoreItems storeItems);
}
