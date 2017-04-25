package com.hypersocket.migration.helper;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.hypersocket.migration.annotation.LookUpKeys;
import com.hypersocket.migration.exporter.JsonPropertiesAdder;
import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.util.SpringApplicationContextProvider;
import org.apache.commons.beanutils.PropertyUtils;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class MigrationSerializerForLookUpKeys extends StdSerializer<AbstractEntity<Long>> {

	private static final long serialVersionUID = 1003528885274798166L;

	protected MigrationSerializerForLookUpKeys() {
        this(null);
    }

    protected MigrationSerializerForLookUpKeys(Class<AbstractEntity<Long>> t) {
        super(t);
    }

    @Override
    public void serialize(AbstractEntity<Long> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
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
        addAnyPropertiesFromCustomBean(value, gen, provider);
        gen.writeBooleanField("reference", true);
        gen.writeStringField("_meta", value._meta());
        gen.writeEndObject();
    }

    private void addAnyPropertiesFromCustomBean(AbstractEntity<Long> value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        Class<?> aClass = value.getClass();
        String customBean = String.format("%sJsonPropertiesAdder", aClass.getSimpleName());
        if(SpringApplicationContextProvider.getApplicationContext().containsBean(customBean)) {
            JsonPropertiesAdder jsonPropertiesAdder = (JsonPropertiesAdder) SpringApplicationContextProvider.
                    getApplicationContext().getBean(customBean);
            jsonPropertiesAdder.add(value, gen, provider);
        }
    }

    private String[] getLookUpPropertyNames(AbstractEntity<Long> value) {
        LookUpKeys keys = value.getClass().getAnnotation(LookUpKeys.class);
        if(keys == null) {
            throw new IllegalStateException(String.format("Keys not found on applied entity class %s",
                    value.getClass().getCanonicalName()));
        }

        return keys.propertyNames();
    }
}
