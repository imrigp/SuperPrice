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
