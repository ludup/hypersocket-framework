package com.hypersocket.input;

public class ImgField extends InputField {

	String url;
	String alt;
	Integer width;
	String _float;
	String _class;
	
	public ImgField() {
	}

	public ImgField(String resourceKey, String defaultValue) {
		super(InputFieldType.img, resourceKey, defaultValue, true, null);
	}
	
	public ImgField(String resourceKey, String defaultValue, String url) {
		this(resourceKey, defaultValue, url, null, null, null);
	}
	
	public ImgField(String resourceKey, String defaultValue, String url, String alt, Integer width) {
		this(resourceKey, defaultValue, url, alt, width, null);
	}
	
	public ImgField(String resourceKey, String defaultValue, String url, String alt, Integer width, String _class) {
		super(InputFieldType.img, resourceKey, defaultValue, true, null);
		this.url = url;
		this.alt = alt;
		this.width = width;
		this._class = _class;
	}
	
	public String getUrl() {
		return url;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getStyleClass() {
		return _class;
	}
}
