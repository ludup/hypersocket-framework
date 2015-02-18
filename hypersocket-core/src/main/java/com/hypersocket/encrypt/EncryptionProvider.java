package com.hypersocket.encrypt;



public interface EncryptionProvider {

	String encrypt(String toEncrypt) throws Exception;

	String decrypt(String toDecrypt) throws Exception;

}
