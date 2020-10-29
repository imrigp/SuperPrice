/*
 *
 *  * Copyright 2020 Imri
 *  *
 *  * This application is free software; you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

package database;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.sql.DataSource;

import org.jetbrains.annotations.NotNull;
import server.entities.Chain;
import server.entities.Item;
import server.entities.ItemPrice;
import server.entities.Store;
import server.entities.StoreItems;

public final class DbQuery implements ChainDao, StoreItemsDao, StoreDao, ItemDao, ItemPriceDao {

    private static final boolean DB_OFF = false; // For debugging
    private static DbQuery instance;
    private static final DataSource ds = Database.getDataSource();
    private static final ChainDao chainDao = new ChainDaoPostgres(ds);
    private static final StoreDao storeDao = new StoreDaoPostgres(ds);
    private static final StoreItemsDao storeItemsDao = new StoreItemsDaoPostgres(ds);
    private static final ItemDao itemDao = new ItemDaoPostgres(ds);
    private static final ItemPriceDao itemPriceDao = new ItemPriceDaoPostgres(ds);

    private DbQuery() {
        if (instance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    public static DbQuery getInstance() {
        if (instance == null) {
            instance = new DbQuery();
        }
        return instance;
    }

    public List<String> getParsedFiles() {
        if (DB_OFF) {
            return Collections.emptyList();
        }
        return Database.getParsedFiles();
    }

    public void addParsedFile(String name) {
        if (DB_OFF) {
            return;
        }
        Database.addParsedFile(name);
    }

    public void updateMeasures(Set<String> measures) {
        if (DB_OFF) {
            return;
        }
        Database.updateMeasures(measures);
    }

    public void clearTables() {
        if (DB_OFF) {
            return;
        }
        Database.clearTables();
    }

    @Override
    public List<Chain> getAllChains() {
        if (DB_OFF) {
            return Collections.emptyList();
        }
        return chainDao.getAllChains();
    }

    @Override
    public Chain getChain(long id) {
        if (DB_OFF) {
            return null;
        }
        return chainDao.getChain(id);
    }

    @Override
    public void addChain(Chain chain) {
        if (DB_OFF) {
            return;
        }
        chainDao.addChain(chain);
    }

    @Override
    public List<Item> searchItems(String name) {
        if (DB_OFF) {
            return Collections.emptyList();
        }
        return itemDao.searchItems(name);
    }

    @Override
    public List<Item> getAllItems() {
        if (DB_OFF) {
            return Collections.emptyList();
        }
        return itemDao.getAllItems();
    }

    @Override
    public List<Item> getIncompleteItems() {
        if (DB_OFF) {
            return Collections.emptyList();
        }
        return itemDao.getIncompleteItems();
    }

    @Override
    public Item getItem(long id) {
        if (DB_OFF) {
            return null;
        }
        return itemDao.getItem(id);
    }

    @Override
    public void addItems(List<Item> items) {
        if (DB_OFF) {
            return;
        }
        itemDao.addItems(items);
    }

    @Override
    public void updateItem(Item item) {
        if (DB_OFF) {
            return;
        }
        itemDao.updateItem(item);
    }

    @Override
    public List<Store> getChainStores(long chainId) {
        if (DB_OFF) {
            return Collections.emptyList();
        }
        return storeDao.getChainStores(chainId);
    }

    @Override
    public Map<String, List<Store>> searchStores(String city, String groupBy) {
        return storeDao.searchStores(city, groupBy);
    }

    @Override
    public List<Store> searchStores(String city) {
        return storeDao.searchStores(city);
    }

    @Override
    public Store getStore(long chainId, int storeId) {
        if (DB_OFF) {
            return null;
        }
        return storeDao.getStore(chainId, storeId);
    }

    @Override
    public void addStore(long chainId, Store store) {
        if (DB_OFF) {
            return;
        }
        storeDao.addStore(chainId, store);
    }

    @Override
    public void addStores(long chainId, Map<Integer, Store> stores) {
        if (DB_OFF) {
            return;
        }
        storeDao.addStores(chainId, stores);
    }

    @Override
    public StoreItems getStoreItems(long chainId, int storeId) {
        if (DB_OFF) {
            return null;
        }
        return storeItemsDao.getStoreItems(chainId, storeId);
    }

    @Override
    public void addStoreItems(StoreItems storeItems) {
        if (DB_OFF) {
            return;
        }
        storeItemsDao.addStoreItems(storeItems);
    }

    @NotNull
    @Override
    public List<ItemPrice> getItemsPrice(long chainId, int storeId, List<Long> itemIds) {
        if (DB_OFF) {
            return Collections.emptyList();
        }
        return itemPriceDao.getItemsPrice(chainId, storeId, itemIds);
    }
}
