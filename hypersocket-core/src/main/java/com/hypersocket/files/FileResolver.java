package com.hypersocket.files;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.hypersocket.realm.Realm;

public interface FileResolver {

	boolean isSystem();

	Integer getWeight();

	boolean fileExists(String path, Realm realm);

	InputStream getInputStream(String path, Realm realm) throws IOException;

	OutputStream getOutputStream(String path, Realm realm) throws IOException;

}
