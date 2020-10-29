package server.entities.serializers;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import server.entities.Chain;

public class ChainSerializer extends StdSerializer<Chain> {

    public ChainSerializer() {
        this(null);
    }

    public ChainSerializer(Class<Chain> t) {
        super(t);
    }

    @Override
    public void serialize(Chain value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeNumberField("id", value.getId());
        gen.writeStringField("name", value.getName());
        gen.writeEndObject();
    }
}
