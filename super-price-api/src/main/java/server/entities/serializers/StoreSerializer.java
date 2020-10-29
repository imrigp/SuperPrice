package server.entities.serializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import server.entities.Store;

public class StoreSerializer extends StdSerializer<Store> {

    public StoreSerializer() {
        this(null);
    }

    public StoreSerializer(Class<Store> t) {
        super(t);
    }

    @Override
    public void serialize(Store value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("id", value.getStoreId());
        gen.writeNumberField("chain_id", value.getChainId());
        gen.writeStringField("name", value.getName());
        gen.writeStringField("address", value.getAddress());
        gen.writeStringField("city", value.getCity());
        gen.writeEndObject();
    }
}