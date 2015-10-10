package com.hypersocket.secret;

import java.io.IOException;

import com.hypersocket.encrypt.EncryptionProvider;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceNotFoundException;

public interface SecretKeyService extends AbstractResourceService<SecretKeyResource> {

	SecretKeyResource createSecretKey(Realm realm) throws ResourceCreationException, AccessDeniedException;

	SecretKeyResource createSecretKey(String name, Realm realm) throws ResourceCreationException,
			AccessDeniedException;

	byte[] generateSecreyKeyData(SecretKeyResource key) throws IOException;

	byte[] generateIvData(SecretKeyResource key) throws IOException;

	SecretKeyResource getSecretKey(String reference, Realm realm) throws ResourceNotFoundException, ResourceCreationException, AccessDeniedException;

	void setEncryptorProvider(EncryptionProvider encryptionProvider);

	EncryptionProvider getEncryptorProvider();

}
