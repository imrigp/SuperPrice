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

import java.util.List;
import java.util.Map;

import server.entities.Store;

public interface StoreDao {
    List<Store> getChainStores(long chainId);

    Map<String, List<Store>> searchStores(String city, String groupBy);

    List<Store> searchStores(String city);

    Store getStore(long chainId, int storeId);

    void addStore(long chainId, Store store);

    void addStores(long chainId, Map<Integer, Store> stores);
}
