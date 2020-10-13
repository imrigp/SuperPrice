package server.entities;

import java.util.HashMap;
import java.util.Map;

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
