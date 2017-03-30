package com.hypersocket.migration.helper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.hypersocket.resource.Resource;

import java.io.IOException;

public class MigrationSerializerForResource extends StdSerializer<Resource> {

    protected MigrationSerializerForResource() {
        this(null);
    }

    protected MigrationSerializerForResource(Class<Resource> t) {
        super(t);
    }

    @Override
    public void serialize(Resource value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("name", value.getName());
        gen.writeNumberField("_legacyId", value.getLegacyId());
        gen.writeStringField("_meta", value._meta());
        gen.writeEndObject();
    }
}
