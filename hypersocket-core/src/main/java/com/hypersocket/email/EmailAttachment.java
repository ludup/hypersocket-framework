package com.hypersocket.email;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

public class EmailAttachment implements DataSource {

	String filename;
	String contentType;
	File file;
	
	public EmailAttachment(String filename, String contentType, File file) {
		super();
		this.filename = filename;
		this.contentType = contentType;
		this.file = file;
	}
	
	public String getFilename() {
		return filename;
	}
	
	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(file);
	}

	@Override
	public String getName() {
		return filename;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		throw new UnsupportedOperationException();
	}
	
	

}
