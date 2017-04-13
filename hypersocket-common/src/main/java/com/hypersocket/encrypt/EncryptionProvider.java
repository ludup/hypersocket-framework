package com.hypersocket.encrypt;

import java.io.IOException;
import java.security.Provider;

import javax.crypto.SecretKey;



public interface EncryptionProvider {

	String encrypt(String toEncrypt) throws Exception;

	String decrypt(String toDecrypt) throws Exception;

	boolean supportsSecretKeyStorage();

	void createSecretKey(String reference) throws IOException;

	SecretKey getSecretKey(String reference) throws IOException;

	Provider getProvider();

	String getName();

}
