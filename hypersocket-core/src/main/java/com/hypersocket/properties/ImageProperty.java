package com.hypersocket.properties;

public class ImageProperty implements Property {

	private String resourceKey;
	private String value;
	
	ImageProperty(String resourceKey, String value) {
		this.resourceKey = resourceKey;
		this.value = value;
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String getResourceKey() {
		return resourceKey;
	}

}
