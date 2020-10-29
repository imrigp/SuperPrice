/*
 *
 *  * Copyright 2020 Imri
 *  *
 *  * This application is free software; you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This software is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 *
 */

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