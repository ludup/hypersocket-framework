package com.hypersocket.json;

import java.io.File;

public class MultipartObject {
	private String property;
	private File file;

	public MultipartObject(String property, File file) {
		super();
		this.property = property;
		this.file = file;
	}

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}
}
