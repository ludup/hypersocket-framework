package com.hypersocket.encrypt;

import org.springframework.stereotype.Service;

import com.hypersocket.realm.Realm;

@Service
public interface EncryptionService extends Encryptor {

	void setEncryptor(Encryptor encryptor);

	Encryptor getEncryptor();

	Realm getSystemRealm();

}
