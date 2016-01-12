package com.hypersocket.properties;

public class NameValuePair {

	String name;
	String value;
	
	public NameValuePair() {
		
	}
	
	public NameValuePair(String name, String value) {
		this.name = name;
		this.value = value;
	}
	
	public NameValuePair(String pair) {
		this.name = ResourceUtils.getNamePairKey(pair);
		this.value = ResourceUtils.getNamePairValue(pair);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	
}
