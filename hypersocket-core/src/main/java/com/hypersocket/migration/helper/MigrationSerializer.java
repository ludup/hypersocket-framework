package com.hypersocket.migration.helper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.hypersocket.resource.Resource;

import java.io.IOException;

public class MigrationSerializer extends StdSerializer<Resource> {

    protected MigrationSerializer() {
        this(null);
    }

    protected MigrationSerializer(Class<Resource> t) {
        super(t);
    }

    @Override
    public void serialize(Resource value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("name", value.getName());
        gen.writeStringField("_meta", value._meta());
        gen.writeEndObject();
    }
}
