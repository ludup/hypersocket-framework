package com.hypersocket.encrypt;

import java.io.IOException;

import com.hypersocket.realm.Realm;

public interface Encryptor {

	String decryptString(String reference, String data, Realm realm) throws IOException;

	String encryptString(String reference, String data, Realm realm)
			throws IOException;
	
	String getProviderName();
}
