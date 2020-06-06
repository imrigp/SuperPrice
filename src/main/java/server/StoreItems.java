package server;

import java.util.ArrayList;

public class StoreItems {
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
