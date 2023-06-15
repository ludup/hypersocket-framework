package com.hypersocket.encrypt;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.hypersocket.ApplicationContextServiceImpl;
import com.hypersocket.context.AuthenticatedContext;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.transactions.TransactionService;

@Service
public class EncryptionServiceImpl implements EncryptionService {

	static Logger log = LoggerFactory.getLogger(EncryptionServiceImpl.class);
	
	@Autowired
	@Qualifier("defaultEncryptor")
	@Lazy
	private Encryptor encryptor;

	@Autowired
	private TransactionService transactionService; 
	
	@Override
	public String encryptString(String reference, String data, Realm realm) throws IOException {

		log.info("REMOVEME about to encrypt (using {}) data of {} bytes for reference of {} on realm {}", encryptor.getClass().getName(), data.getBytes().length, reference, realm.getName());
		
		return ResourceUtils.addEncryptedTag(encryptor.encryptString(reference, data, realm));
	}
	
	@Override
	public String decryptString(String reference, String data, Realm realm) throws IOException{
		log.info("REMOVEME about to decrypt (using {}) data of {} bytes for reference of {} on realm {}", encryptor.getClass().getName(),data.getBytes().length, reference, realm.getName());	
		return encryptor.decryptString(reference, ResourceUtils.removeEncryptedTag(data), realm);
	}

	@Override
	@AuthenticatedContext(system = true)
	@EventListener
	public void onContextStartedEvent(ContextStartedEvent event) {
		
		try {
			transactionService.doInTransaction(new TransactionCallback<Object>() {

				@Override
				public Object doInTransaction(TransactionStatus status) {

					try {
						RealmService realmService = ApplicationContextServiceImpl.getInstance().getBean(RealmService.class);
						String text = encryptString("Test Key", "Encryption service has been initialized", realmService.getDefaultRealm());
						log.info(getProviderName() + " " + decryptString("Test Key", text, realmService.getDefaultRealm()) + " " + text);
					} catch (Exception e) {
						log.error("Failed to process test encryption key", e);
					}

					return null;
				}
			});
		} catch (ResourceException e) {
			log.error("Failed to initialize encryption service", e);
		} catch (AccessDeniedException e) {
			log.error("Failed to initialize encryption service", e);
		}
		
	}

	@Override
	public Encryptor setEncryptor(Encryptor encryptor) {
		var was = this.encryptor;
		this.encryptor = encryptor;
		return was;
	}

	public String getProviderName() {
		return encryptor.getProviderName();
	}
}
