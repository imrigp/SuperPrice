package server;

import java.util.ArrayList;

public class Chain {
    private long id;
    private String name;
    private ArrayList<Store> stores;

    public Chain() {
        stores = new ArrayList<>();
    }

    public Chain(long id, String name) {
        this();
        this.id = id;
        this.name = name;
    }

    public void addStore(Store store) {
        stores.add(store);
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

    public ArrayList<Store> getStores() {
        return stores;
    }

    public void setStores(ArrayList<Store> stores) {
        this.stores = stores;
    }

    @Override
    public String toString() {
        return name;
    }
}
