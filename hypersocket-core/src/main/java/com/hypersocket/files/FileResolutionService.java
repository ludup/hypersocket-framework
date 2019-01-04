package com.hypersocket.files;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.hypersocket.realm.Realm;

public interface FileResolutionService {

	boolean fileExists(String path, Realm realm, boolean includeSystem);

	InputStream getInputStream(String path, Realm realm, boolean includeSystem) throws IOException;

	OutputStream getOutputStream(String path, Realm realm, boolean includeSystem) throws IOException;

	void registerFileSource(FileResolver source);

}
