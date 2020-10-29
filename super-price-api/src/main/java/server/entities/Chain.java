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

package server.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import server.entities.serializers.ChainSerializer;

@JsonSerialize(using = ChainSerializer.class)
public class Chain extends Entity {
    private long id;
    private String name;
    private Map<Integer, Store> stores;

    public Chain() {
        stores = new HashMap<>();
    }

    public Chain(long id, String name) {
        this();
        this.id = id;
        this.name = name;
    }

    public void addStore(Store store) {
        stores.put(store.getStoreId(), store);
    }

    public void addStores(List<Store> stores) {
        stores.forEach(this::addStore);
    }

    public long getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Long.parseLong(id);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<Integer, Store> getStores() {
        return stores;
    }

    public void setStores(Map<Integer, Store> stores) {
        this.stores = stores;
    }

    @Override
    public String toString() {
        return name;
    }
}
