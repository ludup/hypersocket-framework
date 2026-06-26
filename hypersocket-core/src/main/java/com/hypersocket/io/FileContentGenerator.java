package com.hypersocket.io;

import java.io.IOException;
import java.io.OutputStream;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;

public interface FileContentGenerator {
	void generate(OutputStream out, Realm realm) throws IOException, AccessDeniedException;
}
