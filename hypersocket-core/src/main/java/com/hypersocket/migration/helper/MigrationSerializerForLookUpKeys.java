package com.hypersocket.migration.helper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.hypersocket.migration.annotation.LookUpKeys;
import com.hypersocket.repository.AbstractEntity;
import org.apache.commons.beanutils.PropertyUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class MigrationSerializerForLookUpKeys extends StdSerializer<AbstractEntity> {

    protected MigrationSerializerForLookUpKeys() {
        this(null);
    }

    protected MigrationSerializerForLookUpKeys(Class<AbstractEntity> t) {
        super(t);
    }

    @Override
    public void serialize(AbstractEntity value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        String[] propertyNames = getLookUpPropertyNames(value);
        gen.writeStartObject();
        for(String property : propertyNames) {
            try {
                Object propertyValue = PropertyUtils.getProperty(value, property);
                if(propertyValue instanceof Number) {
                    gen.writeNumberField(property, (Long) propertyValue);
                } else {
                    gen.writeStringField(property, (String) propertyValue);
                }
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }
        }
        gen.writeStringField("_meta", value._meta());
        gen.writeEndObject();
    }

    private String[] getLookUpPropertyNames(AbstractEntity value) {
        LookUpKeys keys = value.getClass().getAnnotation(LookUpKeys.class);
        if(keys == null) {
            throw new IllegalStateException(String.format("Keys not found on applied entity class %s",
                    value.getClass().getCanonicalName()));
        }

        return keys.propertyNames();
    }
}
