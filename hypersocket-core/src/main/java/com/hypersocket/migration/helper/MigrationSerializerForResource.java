package com.hypersocket.migration.helper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.hypersocket.migration.exporter.JsonPropertiesAdder;
import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.resource.Resource;
import com.hypersocket.util.SpringApplicationContextProvider;

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
        gen.writeNumberField("legacyId", value.getLegacyId());
        gen.writeStringField("_meta", value._meta());
        addAnyPropertiesFromCustomBean(value, gen, provider);
        gen.writeBooleanField("reference", true);
        gen.writeEndObject();
    }

    private void addAnyPropertiesFromCustomBean(AbstractEntity value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        Class aClass = value.getClass();
        String customBean = String.format("%sJsonPropertiesAdder", aClass.getSimpleName());
        if(SpringApplicationContextProvider.getApplicationContext().containsBean(customBean)) {
            JsonPropertiesAdder jsonPropertiesAdder = (JsonPropertiesAdder) SpringApplicationContextProvider.
                    getApplicationContext().getBean(customBean);
            jsonPropertiesAdder.add(value, gen, provider);
        }
    }
}
