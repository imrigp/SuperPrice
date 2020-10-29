package server.entities.serializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import server.entities.ItemPrice;

public class ItemPriceSerializer extends StdSerializer<ItemPrice> {
    public ItemPriceSerializer() {
        this(null);
    }

    public ItemPriceSerializer(Class<ItemPrice> t) {
        super(t);
    }

    @Override
    public void serialize(ItemPrice value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("item_id", value.getItemId());
        gen.writeNumberField("price", value.getPrice());
        gen.writeEndObject();
    }
}
