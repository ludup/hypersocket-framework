package com.hypersocket.input;

public class HtmlField extends InputField {

	public HtmlField() {
	}

	public HtmlField(String resourceKey, String defaultValue) {
		super(InputFieldType.html, resourceKey, defaultValue, true, null);
	}

}
