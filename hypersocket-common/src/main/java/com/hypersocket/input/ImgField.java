package com.hypersocket.input;

public class ImgField extends InputField {

	String url;
	
	public ImgField() {
	}

	public ImgField(String resourceKey, String defaultValue) {
		super(InputFieldType.img, resourceKey, defaultValue, true, null);
	}
	
	public ImgField(String resourceKey, String defaultValue, String url) {
		super(InputFieldType.img, resourceKey, defaultValue, true, null);
		this.url = url;
	}
	
	public String getUrl() {
		return url;
	}
}
