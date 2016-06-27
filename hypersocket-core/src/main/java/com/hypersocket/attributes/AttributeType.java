package com.hypersocket.attributes;

public enum AttributeType {

	TEXT("text"),
	PASSWORD("password"),
	SWITCH("switch"),
	CHECKBOX("checkbox"),
	SELECT("select"),
	MULTIPLE_TEXT("multipleTextInput");

	AttributeType(String inputType) {
		this.inputType = inputType;
	}
	private String inputType;
	
	public String getInputType() {
		return inputType;
	}
}
