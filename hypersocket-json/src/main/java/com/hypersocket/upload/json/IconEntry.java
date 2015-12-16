package com.hypersocket.upload.json;

public class IconEntry {

	String value;
	String name;
	String icon;

	public IconEntry() {
	}

	public IconEntry(String value, String name) {
		this.value = value;
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	void setValue(String id) {
		this.value = id;
	}

	public String getName() {
		return name;
	}

	void setName(String name) {
		this.name = name;
	}

	public String getIcon() {
		return icon;
	}

	void setIcon(String icon) {
		this.icon = icon;
	}

}
