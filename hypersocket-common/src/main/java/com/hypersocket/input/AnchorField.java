package com.hypersocket.input;

public class AnchorField extends InputField {

	public AnchorField() {
	}

	public AnchorField(String resourceKey,
			String defaultValue, boolean required, String label) {
		super(InputFieldType.a, resourceKey, defaultValue, required, label);
	}

}
