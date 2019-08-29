package com.hypersocket.secret;

import java.io.IOException;

import com.hypersocket.encrypt.EncryptionProvider;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;

public interface SecretKeyService extends AbstractResourceService<SecretKeyResource> {

	SecretKeyResource createSecretKey(Realm realm) throws AccessDeniedException, ResourceException;

	SecretKeyResource createSecretKey(String name, Realm realm) throws
			AccessDeniedException, ResourceException;

	byte[] generateSecreyKeyData(SecretKeyResource key) throws IOException;

	byte[] generateIvData(SecretKeyResource key) throws IOException;

	SecretKeyResource getSecretKey(String reference, Realm realm) throws ResourceException, AccessDeniedException;

	void setEncryptorProvider(EncryptionProvider encryptionProvider);

	EncryptionProvider getEncryptorProvider();

	void deleteSecretKey(String name, Realm realm) throws ResourceNotFoundException, ResourceException, AccessDeniedException;

}
