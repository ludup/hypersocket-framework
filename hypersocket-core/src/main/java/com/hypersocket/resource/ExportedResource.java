package com.hypersocket.resource;

import java.util.HashMap;
import java.util.Map;

public class ExportedResource {

	Map<String,String> properties = new HashMap<String,String>();
	Resource resource;
	
	public ExportedResource() {
		
	}

	public Map<String, String> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, String> properties) {
		this.properties = properties;
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	
}
