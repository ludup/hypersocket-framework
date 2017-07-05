package com.hypersocket.migration.mapper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonDeserializer.None;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.cfg.MapperConfig;
import com.fasterxml.jackson.databind.introspect.Annotated;
import com.fasterxml.jackson.databind.jsontype.TypeIdResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.hypersocket.migration.helper.MigrationDeserializer;

@Component
public class MigrationHandlerInstantiator extends HandlerInstantiator {

    @Autowired
    MigrationDeserializer migrationDeserializer;

    @Override
    public JsonDeserializer<?> deserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> deserClass) {
    	try{
    		if(MigrationDeserializer.class.equals(deserClass)) {
       		 	return migrationDeserializer;
    		} else if(None.class.equals(deserClass)) {
    			return null;
    		}
    		
    		System.out.println("DeserializationConfig " + config);
    		System.out.println("Annotated " + annotated);
    		System.out.println("deserClass Class<?> " + deserClass);
    		
    		return (JsonDeserializer<?>) deserClass.newInstance();
    	}catch (Exception e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
    }

    @Override
    public KeyDeserializer keyDeserializerInstance(DeserializationConfig config, Annotated annotated, Class<?> keyDeserClass) {
        return null;
    }

    @Override
    public JsonSerializer<?> serializerInstance(SerializationConfig config, Annotated annotated, Class<?> serClass) {
        return null;
    }

    @Override
    public TypeResolverBuilder<?> typeResolverBuilderInstance(MapperConfig<?> config, Annotated annotated, Class<?> builderClass) {
        return null;
    }

    @Override
    public TypeIdResolver typeIdResolverInstance(MapperConfig<?> config, Annotated annotated, Class<?> resolverClass) {
        return null;
    }
}
