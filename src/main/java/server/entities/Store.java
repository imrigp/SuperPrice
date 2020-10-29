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
