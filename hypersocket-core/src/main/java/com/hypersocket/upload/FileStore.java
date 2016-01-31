package com.hypersocket.upload;

import java.io.IOException;
import java.io.InputStream;

import com.hypersocket.realm.Realm;

public interface FileStore {

	long writeFile(Realm realm, String uuid, InputStream in) throws IOException;

}
