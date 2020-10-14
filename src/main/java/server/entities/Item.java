package server.entities;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Item extends Entity {
    private String name;
    private String manufacturerName;
    private String manufactureCountry;
    private QuantityUnit quantityUnit;
    private float quantity;
    private String unitOfMeasure;
    private float price;
    private long id;

    public Item() {
        this.name = "";
        this.manufacturerName = "";
        this.manufactureCountry = "";
        this.quantityUnit = QuantityUnit.UNKNOWN;
        this.quantity = 0;
        this.unitOfMeasure = "";
        this.price = 0;
    }

    public Item(String name, String manufacturerName, String manufactureCountry, String quantityUnit,
                float quantity, String unitOfMeasure, float price, long id) {
        this.name = name;
        this.manufacturerName = manufacturerName;
        this.manufactureCountry = manufactureCountry;
        this.quantityUnit = QuantityUnit.fromString(quantityUnit);
        this.quantity = quantity;
        this.unitOfMeasure = unitOfMeasure;
        this.price = price;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = Float.parseFloat(price);
    }

    public long getId() {
        return id;
    }

    public void setId(String id) {
        this.id = Long.parseLong(id);
    }

    public String getManufacturerName() {
        return manufacturerName;
    }

    public void setManufacturerName(String manufacturerName) {
        if ("לא ידוע".equals(manufacturerName) || "Unknown".equals(manufacturerName)) {
            manufacturerName = null;
        }
        this.manufacturerName = manufacturerName;
    }

    public String getManufactureCountry() {
        return manufactureCountry;
    }

    public void setManufactureCountry(String manufactureCountry) {
        if ("לא ידוע".equals(manufactureCountry) || "Unknown".equals(manufactureCountry)) {
            manufactureCountry = null;
        }

        this.manufactureCountry = manufactureCountry;
    }

    public QuantityUnit getQuantityUnit() {
        return quantityUnit;
    }

    public void setQuantityUnit(QuantityUnit quantityUnit) {
        this.quantityUnit = quantityUnit;
    }

    public void setQuantityUnit(String quantityUnit) {
        this.quantityUnit = QuantityUnit.fromString(quantityUnit);
    }

    public float getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        if (quantity != null) {
            this.quantity = Float.parseFloat(quantity);
        }
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Item item = (Item) o;

        return id == item.id;
    }

    @Override
    public int hashCode() {
        return (int) (id ^ (id >>> 32));
    }

    @Override
    public String toString() {
        return "Id: " + id + "\nName: " + name + "\nPrice: " + price + "\n";
    }

    public enum QuantityUnit {
        GRAM("גרם", "גרמים", "גר", "ג", "ג'", "ג`", "גר'", "גר`"),
        LITER("ליטר", "ליטרים", "ל", "ל'", "ל`"),
        KILO("קילו", "קילוגרם", "קילוגרמים", "קג", "ק", "ק\"ג", "ק'", "ק'ג", "ק`", "ק`ג", "לקג"),
        ML("מיליליטר", "מיליליטרים", "מל", "מ\"ל", "מ", "מ'ל", "מ`ל"),
        CM("סמ", "ס\"מ", "סנטימטר"),
        METER("מטר", "מטרים"),
        UNIT("יחידה", "יח'", "יח`", "יח", "יחי", "יחידו"),
        UNKNOWN();

        final List<String> aliases = new ArrayList<>();

        QuantityUnit(String... aliases) {
            this.aliases.addAll(Arrays.asList(aliases));
        }

        static public QuantityUnit fromString(String key) {
            return ALIAS_MAP.getOrDefault(key, UNKNOWN);
        }

        static final private Map<String, QuantityUnit> ALIAS_MAP = new HashMap<>();

        static {
            for (QuantityUnit value : QuantityUnit.values()) {
                value.aliases.forEach(a -> ALIAS_MAP.put(a, value));
            }
        }
    }
}
