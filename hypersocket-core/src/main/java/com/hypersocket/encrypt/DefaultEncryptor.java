package com.hypersocket.encrypt;

import java.io.IOException;
import java.security.Security;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.secret.SecretKeyResource;
import com.hypersocket.secret.SecretKeyService;

@Component
public class DefaultEncryptor implements Encryptor {

	@Autowired
	SecretKeyService secretKeyService;

	@Override
	public String encryptString(String reference, String data)
			throws IOException {

		try {
			SecretKeyResource key;

			try {
				key = secretKeyService.getResourceByName(reference);
			} catch (ResourceNotFoundException e) {
				key = secretKeyService.createSecretKey(reference);
			}

			SecretKeySpec secretKeySpec = new SecretKeySpec(
					secretKeyService.generateSecreyKeyData(key), "AES");
			byte[] iv = secretKeyService.generateIvData(key);

			Cipher aesCipherForEncryption = Cipher
					.getInstance("AES/CTR/PKCS7PADDING");

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
	public String decryptString(String reference, String data)
			throws IOException {
		try {
			SecretKeyResource key = secretKeyService
					.getResourceByName(reference);

			SecretKeySpec secretKeySpec = new SecretKeySpec(
					secretKeyService.generateSecreyKeyData(key), "AES");
			byte[] iv = secretKeyService.generateIvData(key);

			Cipher aesCipherForDecryption = Cipher
					.getInstance("AES/CTR/PKCS7PADDING");

			aesCipherForDecryption.init(Cipher.DECRYPT_MODE, secretKeySpec,
					new IvParameterSpec(iv));

			byte[] byteDecryptedText = aesCipherForDecryption.doFinal(Base64
					.decodeBase64(data));
			return new String(byteDecryptedText, "UTF-8");
		} catch (Exception e) {
			throw new IOException(e);
		}
	}

}
