package server;


public class Item {
    private String name;
    private String manufacturerName;
    private String manufactureCountry;
    private String unitQty;
    private float qty;
    private String unitOfMeasure;
    private float unitOfMeasurePrice;
    private float price;
    private long id;


    public Item() {
        this.name = "";
        this.manufacturerName = "";
        this.manufactureCountry = "";
        this.unitQty = "";
        this.qty = 0;
        this.unitOfMeasure = "";
        this.unitOfMeasurePrice = 0;
        this.price = 0;
    }

    public Item(String name, String manufacturerName, String manufactureCountry, String unitQty,
                float qty, String unitOfMeasure, float unitOfMeasurePrice, float price, long id) {
        this.name = name;
        this.manufacturerName = manufacturerName;
        this.manufactureCountry = manufactureCountry;
        this.unitQty = unitQty;
        this.qty = qty;
        this.unitOfMeasure = unitOfMeasure;
        this.unitOfMeasurePrice = unitOfMeasurePrice;
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

    public String getUnitQty() {
        return unitQty;
    }

    public void setUnitQty(String unitQty) {
        this.unitQty = unitQty;
    }

    public float getQty() {
        return qty;
    }

    public void setQty(String qty) {
        if (qty != null) {
            this.qty = Float.parseFloat(qty);
        }
    }

    public String getUnitOfMeasure() {
        return unitOfMeasure;
    }

    public void setUnitOfMeasure(String unitOfMeasure) {
        this.unitOfMeasure = unitOfMeasure;
    }

    public float getUnitOfMeasurePrice() {
        return unitOfMeasurePrice;
    }

    public void setUnitOfMeasurePrice(String unitOfMeasurePrice) {
        this.unitOfMeasurePrice = Float.parseFloat(unitOfMeasurePrice);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

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
}
