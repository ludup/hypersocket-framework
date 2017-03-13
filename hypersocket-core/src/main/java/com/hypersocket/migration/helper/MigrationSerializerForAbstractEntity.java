package com.hypersocket.migration.helper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.hypersocket.repository.AbstractEntity;

import java.io.IOException;

public class MigrationSerializerForAbstractEntity extends StdSerializer<AbstractEntity> {

    protected MigrationSerializerForAbstractEntity() {
        this(null);
    }

    protected MigrationSerializerForAbstractEntity(Class<AbstractEntity> t) {
        super(t);
    }

    /**
     * Will add id as look up key, Please note, this will not respect the @{@link com.fasterxml.jackson.annotation.JsonIgnore}
     * defined in the mix in.
     *
     * @param value
     * @param gen
     * @param provider
     * @throws IOException
     */
    @Override
    public void serialize(AbstractEntity value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("id", value.getId() == null ? "-1" : value.getId().toString());
        gen.writeStringField("_meta", value._meta());
        gen.writeEndObject();
    }
}
