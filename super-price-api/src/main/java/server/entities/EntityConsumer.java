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

import java.util.function.Consumer;

import server.DatabaseState;
import server.Utils;

public class EntityConsumer implements Consumer<Entity> {
    DatabaseState state;

    public EntityConsumer(DatabaseState state) {
        this.state = state;
    }

    @Override
    public void accept(Entity entity) {
        if (entity instanceof StoreItems) {
            mergeItems((StoreItems) entity);
        } else if (entity instanceof Chain) {
            mergeChain((Chain) entity);
        } else if (entity instanceof Store) {
            assert false;
        } else {
            throw new RuntimeException("Unknown entity: " + entity.getClass());
        }
        Utils.updateMeasures();
    }

    private void mergeChain(Chain chain) {
        state.addChain(chain);
    }

    private void mergeItems(StoreItems storeItems) {
        long chainId = storeItems.getChainId();
        int storeId = storeItems.getStoreId();

        // Make sure chain and store are present
        if (!state.getChains().containsKey(chainId)
                || !state.getChains().get(chainId).getStores().containsKey(storeId)) {
            return;
        }

        state.addItems(storeItems.getItems());
        state.addStoreItems(storeItems);
    }
}
