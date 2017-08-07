package com.hypersocket.upload;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface FileStore {

	long writeFile(String path, InputStream in) throws IOException;

	InputStream getInputStream(String path) throws IOException;

	OutputStream getOutputStream(String path) throws IOException;
}
