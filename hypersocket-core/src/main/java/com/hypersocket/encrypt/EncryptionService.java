package com.hypersocket.encrypt;

import org.springframework.stereotype.Service;

import com.hypersocket.events.CoreStartedEvent;

@Service
public interface EncryptionService extends Encryptor {

	void setEncryptor(Encryptor encryptor);

	Encryptor getEncryptor();
	
	void onContextStartedEvent(CoreStartedEvent event);

}
