package com.hypersocket.migration.mapper.module;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.deser.BeanDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerBase;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;
import com.hypersocket.migration.mapper.MigrationBeanDeserializer;
import com.hypersocket.migration.execution.stack.MigrationCurrentStack;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MigrationImpExpModule extends Module{

    @Autowired
    MigrationCurrentStack migrationCurrentStack;

    @Override
    public String getModuleName() {
        return "hypersocket-imp-export";
    }

    @Override
    public Version version() {
        return new Version(1, 2, 0, "", "", "");
    }

    @Override
    public void setupModule(SetupContext context) {
        context.addBeanDeserializerModifier(new BeanDeserializerModifier() {

            @Override
            public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
            if(deserializer instanceof BeanDeserializer) {
                return new MigrationBeanDeserializer((BeanDeserializerBase) deserializer, migrationCurrentStack);
            }
            return deserializer;
            }

						/*@Override
						public JsonDeserializer<?> modifyCollectionDeserializer(DeserializationConfig config, CollectionType type, BeanDescription beanDesc, JsonDeserializer<?> deserializer) {
							return new CollectionDeserializer();
						}*/
        });
    }
}
