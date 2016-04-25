package com.hypersocket.encrypt;

import java.io.IOException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.realm.Realm;
import com.hypersocket.secret.SecretKeyResource;
import com.hypersocket.secret.SecretKeyService;

@Component
public class DefaultEncryptor implements Encryptor {

	static Logger log = LoggerFactory.getLogger(DefaultEncryptor.class);
	
	@Autowired
	SecretKeyService secretKeyService;
	
	@Override
	public String encryptString(String reference, String data, Realm realm)
			throws IOException {

		try {
			
			SecretKeyResource key;
			
			key = secretKeyService.getSecretKey(reference, realm);
			if(key==null) {
				key = secretKeyService.createSecretKey(reference, realm);
			}
			SecretKey secretKeySpec = new SecretKeySpec(secretKeyService.generateSecreyKeyData(key), "AES");
			byte[] iv = secretKeyService.generateIvData(key);

			Cipher aesCipherForEncryption = Cipher
					.getInstance("AES/CTR/PKCS7PADDING", "BC");

			aesCipherForEncryption.init(Cipher.ENCRYPT_MODE, secretKeySpec,
					new IvParameterSpec(iv));

			byte[] byteDataToEncrypt = data.getBytes("UTF-8");
			byte[] byteCipherText = aesCipherForEncryption
					.doFinal(byteDataToEncrypt);

			return Base64.encodeBase64String(byteCipherText);
		} catch (Exception e) {
			throw new IOException(e);
		}

	}

	
	@Override
	public String decryptString(String reference, String data, Realm realm)
			throws IOException {
		try {
			if(StringUtils.isBlank(data)) {
				return "";
			}
			
			SecretKeyResource key = secretKeyService.getSecretKey(reference, realm);
			if(key==null) {
				return "";
			}
			byte[] keydata = secretKeyService.generateSecreyKeyData(key);

			SecretKey secretKeySpec = new SecretKeySpec(keydata, "AES");
			
			byte[] iv = secretKeyService.generateIvData(key);

			Cipher aesCipherForDecryption = Cipher
					.getInstance("AES/CTR/PKCS7PADDING", "BC");

			aesCipherForDecryption.init(Cipher.DECRYPT_MODE, secretKeySpec,
					new IvParameterSpec(iv));

			byte[] tmp = Base64.decodeBase64(data);
			byte[] byteDecryptedText = aesCipherForDecryption.doFinal(tmp);
			return new String(byteDecryptedText, "UTF-8");
		} catch (Exception e) {
			throw new IOException(e);
		}
	}
	
	
	public String getProviderName() {
		return "Secret Key Service/" + secretKeyService.getEncryptorProvider().getName();
	}

}
