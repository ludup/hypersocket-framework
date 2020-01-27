package com.hypersocket.json;

public class PropertyObject {
	private String proertyName;
	private String propertyValue;

	public PropertyObject(String proertyName, String propertyValue) {
		super();
		this.proertyName = proertyName;
		this.propertyValue = propertyValue;
	}

	public String getProertyName() {
		return proertyName;
	}

	public void setProertyName(String proertyName) {
		this.proertyName = proertyName;
	}

	public String getPropertyValue() {
		return propertyValue;
	}

	public void setPropertyValue(String propertyValue) {
		this.propertyValue = propertyValue;
	}
}
