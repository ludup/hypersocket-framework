package com.hypersocket.attributes;

public enum AttributeType {

	TEXT("text"),
	PASSWORD("password"),
	SWITCH("switch"),
	CHECKBOX("checkbox"),
	SELECT("select"),
	MULTIPLE_TEXT("multipleTextInput"),
	DATE("date"),
	COLOR("color"),
	TIME("time"),
	SLIDER("slider");

	AttributeType(String inputType) {
		this.inputType = inputType;
	}
	private String inputType;
	
	public String getInputType() {
		return inputType;
	}
}
