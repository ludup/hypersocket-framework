package com.hypersocket.email;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

public class EmailAttachment implements DataSource {

	String filename;
	String contentType;
	InputStream in;
	
	public EmailAttachment(String filename, String contentType, InputStream in) {
		super();
		this.filename = filename;
		this.contentType = contentType;
		this.in = in;
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
		return in;
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
