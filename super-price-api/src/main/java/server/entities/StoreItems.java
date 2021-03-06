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

public class StoreItems extends Entity {
    private int storeId;
    private long chainId;
    private ArrayList<Item> items;

    public StoreItems() {
        items = new ArrayList<>();
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

    public void setChainId(String chainId) {
        this.chainId = Long.parseLong(chainId);
    }

    public void setChainId(long chainId) {
        this.chainId = chainId;
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void setItems(ArrayList<Item> items) {
        this.items = items;
    }

    public void addItem(Item item) {
        items.add(item);
    }
}
