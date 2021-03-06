package com.hypersocket.input;

public class AnchorField extends InputField {

	boolean isLogonApiLink = true;
	
	public AnchorField() {
	}

	public AnchorField(String resourceKey,
			String defaultValue, boolean required, String label) {
		super(InputFieldType.a, resourceKey, defaultValue, required, label);
	}
	
	public AnchorField(String resourceKey,
			String defaultValue, boolean required, String label, boolean isLogonApiLink) {
		super(InputFieldType.a, resourceKey, defaultValue, required, label);
		this.isLogonApiLink = isLogonApiLink;
	}

}
