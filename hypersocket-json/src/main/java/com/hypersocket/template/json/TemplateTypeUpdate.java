package com.hypersocket.template.json;

public class TemplateTypeUpdate {

	String type;
	
	TemplateTypeUpdate(String type) {
		this.type = type;
	}
	
	public String getId() {
		return type;
	}
	
	public String getName() {
		return type;
	}
	
	public String getValue() {
		return type;
	}
	
	public boolean isNameResourceKey() {
		return true;
	}
}
