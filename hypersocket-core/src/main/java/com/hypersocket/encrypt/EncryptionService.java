package com.hypersocket.encrypt;

import org.springframework.stereotype.Service;

@Service
public interface EncryptionService {

	String encryptString(String reference, String data) throws Exception;

	String decryptString(String reference, String data) throws Exception;

}
