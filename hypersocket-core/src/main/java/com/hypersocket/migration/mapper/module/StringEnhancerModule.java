package com.hypersocket.migration.mapper.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.Module;
import com.hypersocket.migration.mapper.serializers.StringDeserializers;
import com.hypersocket.migration.mapper.serializers.StringSerializers;

@Component
public class StringEnhancerModule extends Module {
	
	@Autowired
	StringSerializers stringSerializers;
	
	@Autowired
	StringDeserializers stringDeserializers;

	@Override
	public String getModuleName() {
		return "hypersocket-string-enhancer";
	}

	@Override
	public Version version() {
		return new Version(1, 2, 0, "", "", "");
	}

	@Override
	public void setupModule(SetupContext context) {
		context.addSerializers(stringSerializers);
		context.addDeserializers(stringDeserializers);
	}

}
