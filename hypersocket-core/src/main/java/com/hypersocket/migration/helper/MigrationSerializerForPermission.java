package com.hypersocket.migration.helper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.hypersocket.permissions.Permission;

import java.io.IOException;

public class MigrationSerializerForPermission extends StdSerializer<Permission> {

    protected MigrationSerializerForPermission() {
        this(null);
    }

    protected MigrationSerializerForPermission(Class<Permission> t) {
        super(t);
    }

    @Override
    public void serialize(Permission value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("resourceKey", value.getResourceKey());
        gen.writeStringField("_meta", value._meta());
        gen.writeEndObject();
    }
}
