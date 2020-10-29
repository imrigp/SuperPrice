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

package server;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import database.DbQuery;
import server.entities.Chain;
import server.entities.Item;
import server.entities.Store;
import server.entities.StoreItems;
import server.xml.XmlDownload;
import server.xml.XmlFile;

public final class DatabaseState {

    private static DatabaseState instance;

    private final DbQuery db = DbQuery.getInstance();

    private final Map<Long, Chain> chains;
    private final Map<Long, Item> incompleteDbItems;
    private final Map<Long, Item> allDbItems;
    private final Set<String> parsedFiles;

    private DatabaseState() {
        if (instance != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }

        parsedFiles = ConcurrentHashMap.newKeySet();
        parsedFiles.addAll(db.getParsedFiles());
        chains =
                db.getAllChains()
                  .stream()
                  .collect(Collectors.toConcurrentMap(Chain::getId, Function.identity()));

        for (Map.Entry<Long, Chain> chainEntry : chains.entrySet()) {
            final List<Store> chainStores = db.getChainStores(chainEntry.getKey());
            chainEntry.getValue().addStores(chainStores);
        }

        incompleteDbItems =
                db.getIncompleteItems()
                  .stream()
                  .collect(Collectors.toConcurrentMap(Item::getId, Function.identity()));
        allDbItems =
                db.getAllItems()
                  .stream()
                  //.filter(item -> !incompleteDbItems.containsKey(item.getId()))
                  .collect(Collectors.toConcurrentMap(Item::getId, Function.identity()));
    }

    public static DatabaseState getInstance() {
        if (instance == null) {
            instance = new DatabaseState();
        }
        return instance;
    }

    public void clearState() {
        parsedFiles.clear();
        chains.clear();
        incompleteDbItems.clear();
        allDbItems.clear();
    }

    public void addChain(Chain chain) {
        chains.put(chain.getId(), chain);
        db.addChain(chain);
        db.addStores(chain.getId(), chain.getStores());
    }

    public void addItems(List<Item> items) {
        db.addItems(items);
    }

    public void addStoreItems(StoreItems storeItems) {
        db.addStoreItems(storeItems);
    }

    public Map<Long, Chain> getChains() {
        return Collections.unmodifiableMap(chains);
    }

    public Map<Long, Item> getIncompleteDbItems() {
        return Collections.unmodifiableMap(incompleteDbItems);
    }

    public Map<Long, Item> getAllDbItems() {
        return Collections.unmodifiableMap(allDbItems);
    }

    public boolean isNewFile(String file) {
        return !parsedFiles.contains(file);
    }

    public boolean isNewFile(XmlDownload file) {
        return !parsedFiles.contains(file.toString());
    }

    public boolean isNewFile(XmlFile file) {
        return !parsedFiles.contains(file.toString());
    }

    public void addParsedFile(String file) {
        parsedFiles.add(file);
        db.addParsedFile(file);
    }
}
