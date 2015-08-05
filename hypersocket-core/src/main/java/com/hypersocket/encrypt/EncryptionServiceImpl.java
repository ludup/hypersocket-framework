package com.hypersocket.encrypt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;

import com.hypersocket.i18n.I18NService;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
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
	
	@Autowired
	@Qualifier("defaultEncryptor")
	Encryptor encryptor;


	@Override
	public String encryptString(String reference, String data, Realm realm) throws IOException {
		
		return encryptor.encryptString(reference, data, realm);

	}
	
	@Override
	public String decryptString(String reference, String data, Realm realm) throws IOException{
		
		return encryptor.decryptString(reference, data, realm);
	}


	@Override
	public void onApplicationEvent(ContextStartedEvent arg0) {
		
		secretKeyService.setCurrentPrincipal(realmService.getSystemPrincipal(), i18nService.getDefaultLocale(), realmService.getDefaultRealm());
		try {
			String text = encryptString("Test Key", "Encryption service has been initialized", realmService.getDefaultRealm());
			log.info(decryptString("Test Key", text, realmService.getDefaultRealm()) + " " + text);
		} catch (Exception e) {
			log.error("Failed to process test encryption key", e);
		}
		secretKeyService.clearPrincipalContext();
	}

	@Override
	public void setEncryptor(Encryptor encryptor) {
		this.encryptor = encryptor;
	}
	
	@Override
	public Encryptor getEncryptor() {
		return encryptor;
	}

	public EncryptionProvider getEncryptionProvider() {
		return encryptionProvider;
	}
}
