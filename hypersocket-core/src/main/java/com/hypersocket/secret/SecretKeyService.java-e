package com.hypersocket.secret;

import java.io.IOException;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceNotFoundException;

public interface SecretKeyService extends AbstractResourceService<SecretKeyResource> {

	SecretKeyResource createSecretKey() throws ResourceCreationException, AccessDeniedException;

	SecretKeyResource createSecretKey(String name) throws ResourceCreationException,
			AccessDeniedException;

	byte[] generateSecreyKeyData(SecretKeyResource key) throws IOException;

	byte[] generateIvData(SecretKeyResource key) throws IOException;

}
