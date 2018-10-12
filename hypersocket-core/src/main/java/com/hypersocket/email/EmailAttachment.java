package com.hypersocket.email;

import java.io.IOException;
import java.io.OutputStream;

import javax.activation.DataSource;

public abstract class EmailAttachment implements DataSource {

	String filename;
	String contentType;
	
	public EmailAttachment(String filename, String contentType) {
		super();
		this.filename = filename;
		this.contentType = contentType;
	}
	
	public String getFilename() {
		return filename;
	}
	
	@Override
	public String getContentType() {
		return contentType;
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
