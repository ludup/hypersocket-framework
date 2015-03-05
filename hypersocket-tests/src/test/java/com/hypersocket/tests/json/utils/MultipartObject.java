package com.hypersocket.tests.json.utils;

import java.io.File;

public class MultipartObject {
	String property;
	File   file;
	
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
