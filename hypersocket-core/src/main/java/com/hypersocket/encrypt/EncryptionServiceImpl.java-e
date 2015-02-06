package com.hypersocket.encrypt;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;

import com.hypersocket.i18n.I18NService;
import com.hypersocket.nss.NssEncryptionProvider;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.rsa.RsaEncryptionProvider;
import com.hypersocket.secret.SecretKeyResource;
import com.hypersocket.secret.SecretKeyService;

public class EncryptionServiceImpl implements EncryptionService, ApplicationListener<ContextStartedEvent> {

	static Logger log = LoggerFactory.getLogger(EncryptionServiceImpl.class);
	
	EncryptionProvider encryptionProvider;
	
	@Autowired
	SecretKeyService secretKeyService;
	
	@Autowired
	RealmService realmService; 
	
	@Autowired
	I18NService i18nService; 
	
	@PostConstruct
	private void postConstruct() throws Exception {
		try {
			encryptionProvider = NssEncryptionProvider.getInstance();
		} catch (Exception e) {
			encryptionProvider = RsaEncryptionProvider.getInstance();
		} 

	}
	
	
	@Override
	public String encryptString(String reference, String data) throws Exception {
		
		SecretKeyResource key;
		
		try {
			key = secretKeyService.getResourceByName(reference);
		} catch (ResourceNotFoundException e) {
			key = secretKeyService.createSecretKey(reference);
		}

		SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyService.generateSecreyKeyData(key), "AES");
		byte[] iv = secretKeyService.generateIvData(key);

		Cipher aesCipherForEncryption = Cipher.getInstance("AES/CTR/PKCS7PADDING");

		aesCipherForEncryption.init(Cipher.ENCRYPT_MODE, secretKeySpec, 
				new IvParameterSpec(iv));

		byte[] byteDataToEncrypt = data.getBytes("UTF-8");
		byte[] byteCipherText = aesCipherForEncryption.doFinal(byteDataToEncrypt);
		
		return Base64.encodeBase64String(byteCipherText);
		
	}
	
	@Override
	public String decryptString(String reference, String data) throws Exception{
		
		SecretKeyResource key = secretKeyService.getResourceByName(reference);
 
		SecretKeySpec secretKeySpec = new SecretKeySpec(secretKeyService.generateSecreyKeyData(key), "AES");
		byte[] iv = secretKeyService.generateIvData(key);

		Cipher aesCipherForDecryption = Cipher.getInstance("AES/CTR/PKCS7PADDING");			

		aesCipherForDecryption.init(Cipher.DECRYPT_MODE, secretKeySpec,
				new IvParameterSpec(iv));
		
		byte[] byteDecryptedText = aesCipherForDecryption
				.doFinal(Base64.decodeBase64(data));
		return new String(byteDecryptedText, "UTF-8");
		
	}


	@Override
	public void onApplicationEvent(ContextStartedEvent arg0) {
		
		secretKeyService.setCurrentPrincipal(realmService.getSystemPrincipal(), i18nService.getDefaultLocale(), realmService.getDefaultRealm());
		try {
			log.info(decryptString("Test Key", encryptString("Test Key", "Encryption service has been initialized")));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		secretKeyService.clearPrincipalContext();
	}

}
