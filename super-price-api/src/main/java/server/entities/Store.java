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

import java.util.ArrayList;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import server.entities.serializers.StoreSerializer;

@JsonSerialize(using = StoreSerializer.class)
public class Store extends Entity {
    private int storeId;
    private long chainId;
    private String name;
    private String address;
    private String city;
    private int type;
    private ArrayList<Item> items;

    public Store() {
        items = new ArrayList<>();
    }

    public Store(int storeId, long chainId, String name, String address, String city, int type) {
        this.storeId = storeId;
        this.chainId = chainId;
        this.name = name;
        this.address = address;
        this.city = city;
        this.type = type;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(String storeId) {
        this.storeId = Integer.parseInt(storeId);
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public long getChainId() {
        return chainId;
    }

    public void setChainId(long chainId) {
        this.chainId = chainId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getType() {
        return type;
    }

    public void setType(String type) {
        if (type != null) {
            this.type = Integer.parseInt(type);
        }
    }

    public void setType(int type) {
        this.type = type;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return name;
    }
}
