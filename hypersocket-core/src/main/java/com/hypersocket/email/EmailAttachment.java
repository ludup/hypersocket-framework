package com.hypersocket.email;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

public class EmailAttachment implements DataSource {

	String filename;
	String contentType;
	InputStream data;
	
	public EmailAttachment(String filename, String contentType, InputStream data) {
		super();
		this.filename = filename;
		this.contentType = contentType;
		this.data = data;
	}
	
	public String getFilename() {
		return filename;
	}
	
	public InputStream getData() {
		return data;
	}

	@Override
	public String getContentType() {
		return contentType;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return data;
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
