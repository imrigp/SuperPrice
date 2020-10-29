package server.entities.serializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import server.entities.Item;

public class ItemSerializer extends StdSerializer<Item> {

    public ItemSerializer() {
        this(null);
    }

    public ItemSerializer(Class<Item> t) {
        super(t);
    }

    @Override
    public void serialize(Item value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("id", value.getId());
        gen.writeStringField("name", value.getName());
        gen.writeStringField("manufacturer_name", value.getManufacturerName());
        gen.writeStringField("manufacture_country", value.getManufactureCountry());
        gen.writeStringField("unit_quantity", value.getQuantityUnit().name());
        gen.writeNumberField("quantity", value.getQuantity());
        gen.writeEndObject();
    }
}