package com.hypersocket.properties;


public class PropertiesFileProperty implements Property {

	PropertyTemplate template;
	String value;
	
	PropertiesFileProperty(PropertyTemplate template, String value) {
		this.template = template;
		this.value = value;
	}
	
	@Override
	public String getValue() {
		return value;
	}

	@Override
	public String getResourceKey() {
		return template.getResourceKey();
	}

	public boolean isHidden() {
		return template.isHidden();
	}

	public String getDefaultValue() {
		return template.getDefaultValue();
	}

	public int getWeight() {
		return template.getWeight();
	}

}
