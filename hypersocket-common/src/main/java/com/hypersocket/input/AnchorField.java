package com.hypersocket.input;

public class AnchorField extends InputField {

	private boolean isLogonApiLink = true;

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
	
	public boolean isLogonApiLink() {
		return isLogonApiLink;
	}

	public void setLogonApiLink(boolean isLogonApiLink) {
		this.isLogonApiLink = isLogonApiLink;
	}
}
