package com.hypersocket.encrypt;

import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Service;

@Service
public interface EncryptionService extends Encryptor {

	Encryptor setEncryptor(Encryptor encryptor);
	
	void onContextStartedEvent(ContextStartedEvent event);

}
