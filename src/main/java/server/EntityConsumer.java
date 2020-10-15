package server;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import server.entities.Chain;
import server.entities.Entity;
import server.entities.Item;
import server.entities.Store;
import server.entities.StoreItems;

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
            System.out.println("Unknown entity: " + entity.getClass());
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

        Map<Long, Item> dbItems = state.getAllDbItems();
        Map<Long, Item> incompleteDbItems = state.getIncompleteDbItems();
        List<Item> items = new ArrayList<>();

        // Add item if it's either in the incomplete list or not in the db at all
        state.addItems(
                storeItems.getItems());
        //.stream()
        //.filter(item -> incompleteDbItems.containsKey(item.getId())
        //        || !dbItems.containsKey(item.getId()))
        //.collect(Collectors.toList()));
        state.addStoreItems(storeItems);
    }
}
