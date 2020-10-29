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

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import server.entities.serializers.ItemPriceSerializer;

@JsonSerialize(using = ItemPriceSerializer.class)
public class ItemPrice extends Entity {
    private long chainId;
    private int storeId;
    private long itemId;
    private float price;

    public ItemPrice(long chainId, int storeId, long itemId, float price) {
        this.chainId = chainId;
        this.storeId = storeId;
        this.itemId = itemId;
        this.price = price;
    }

    public long getChainId() {
        return chainId;
    }

    public void setChainId(long chainId) {
        this.chainId = chainId;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public long getItemId() {
        return itemId;
    }

    public void setItemId(long itemId) {
        this.itemId = itemId;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "ItemPrice{" +
                "chainId=" + chainId +
                ", storeId=" + storeId +
                ", itemId=" + itemId +
                ", price=" + price +
                '}';
    }
}
