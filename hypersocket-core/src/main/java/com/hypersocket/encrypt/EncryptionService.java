package com.hypersocket.encrypt;

import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Service;

import com.hypersocket.realm.Realm;

@Service
public interface EncryptionService extends Encryptor {

	void setEncryptor(Encryptor encryptor);

	Encryptor getEncryptor();

	void onContextStartedEvent(ContextStartedEvent event);

}
