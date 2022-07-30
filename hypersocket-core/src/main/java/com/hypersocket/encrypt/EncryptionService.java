package com.hypersocket.encrypt;

import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Service;

@Service
public interface EncryptionService extends Encryptor {

	void setEncryptor(Encryptor encryptor);

	Encryptor getEncryptor();
	
	void onContextStartedEvent(ContextStartedEvent event);

}
