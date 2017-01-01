package com.hypersocket.input;

public class ImgField extends InputField {

	public ImgField() {
	}

	public ImgField(String resourceKey, String defaultValue) {
		super(InputFieldType.img, resourceKey, defaultValue, true, null);
	}
}
