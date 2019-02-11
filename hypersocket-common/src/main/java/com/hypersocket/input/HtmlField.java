package com.hypersocket.input;

public class HtmlField extends InputField {

	String classes;
	
	public HtmlField() {
	}

	public HtmlField(String resourceKey, String defaultValue) {
		super(InputFieldType.html, resourceKey, defaultValue, true, null);
	}
	
	public HtmlField(String resourceKey, String defaultValue, String classes) {
		super(InputFieldType.html, resourceKey, defaultValue, true, null);
		this.classes = classes;
	}
	
	public String getClasses() {
		return classes;
	}

}
