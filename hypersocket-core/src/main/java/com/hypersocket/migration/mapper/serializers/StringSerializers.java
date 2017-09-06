package com.hypersocket.migration.mapper.serializers;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.Serializers;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.properties.ResourcePropertyStore;
import com.hypersocket.resource.SimpleResource;

@Component
public class StringSerializers extends Serializers.Base {
	
	@Autowired
	SerializersUtil serializersUtil;

	@Override
	public JsonSerializer<?> findSerializer(SerializationConfig config, JavaType type, BeanDescription beanDesc) {
		Class<?> raw = type.getRawClass();
		if(String.class.isAssignableFrom(raw)) {
			return new JsonSerializer<String>() {

				@Override
				public void serialize(String value, JsonGenerator gen, SerializerProvider serializers)
						throws IOException, JsonProcessingException {
					if(gen.getOutputContext().getCurrentValue() instanceof SimpleResource) {
						final SimpleResource resource = (SimpleResource) gen.getOutputContext().getCurrentValue();
						final String currentName = gen.getOutputContext().getCurrentName();
						System.out.println("currentName " + currentName);
						PropertyTemplate propertyTemplate = serializersUtil.getPropertyTemplate(resource, currentName);
						
						if(propertyTemplate != null && propertyTemplate.isEncrypted()) {
							String dcryptedValue = ((ResourcePropertyStore) propertyTemplate.getPropertyStore()).
									getDecryptedValue(propertyTemplate, resource);
							gen.writeString(dcryptedValue);
							return;
						}
					} 
					gen.writeString(value);
				}
			};
		}
		return null;
	}

}
