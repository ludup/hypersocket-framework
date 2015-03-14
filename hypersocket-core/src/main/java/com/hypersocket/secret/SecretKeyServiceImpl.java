package com.hypersocket.secret;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.encrypt.EncryptionProvider;
import com.hypersocket.nss.NssEncryptionProvider;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.rsa.RsaEncryptionProvider;

@Service
public class SecretKeyServiceImpl extends
		AbstractResourceServiceImpl<SecretKeyResource> implements SecretKeyService {

	public static final String RESOURCE_BUNDLE = "SecretKeyService";
	
	static Logger log = LoggerFactory.getLogger(SecretKeyServiceImpl.class);
	
	@Autowired
	SecretKeyRepository repository;
	
	EncryptionProvider encryptionProvider;
	
	public SecretKeyServiceImpl() {
	}

	@PostConstruct
	private void postConstruct() throws Exception {
	
		try {
			encryptionProvider = NssEncryptionProvider.getInstance();
		} catch (Exception e) {
			log.error("Could not create NSS encryption provider", e);
			encryptionProvider = RsaEncryptionProvider.getInstance();
		} 
	}
	
	@Override
	protected AbstractResourceRepository<SecretKeyResource> getRepository() {
		return repository;
	}

	@Override
	protected String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}
	
	@Override
	public SecretKeyResource createSecretKey() throws ResourceCreationException, AccessDeniedException {

		String name = UUID.randomUUID().toString();
		return createSecretKey(name);
	}
	
	@Override
	public SecretKeyResource createSecretKey(String name) throws ResourceCreationException, AccessDeniedException {
		
		SecretKeyResource key = new SecretKeyResource();
		key.setName(name);
		key.setRealm(getCurrentRealm());
		
		createResource(key, new HashMap<String,String>());
		
		return key;
	}
	
	@Override
	public byte[] generateSecreyKeyData(SecretKeyResource key) throws IOException {
		
		try {
			return Hex.decodeHex(encryptionProvider.decrypt(key.getKeydata()).toCharArray());
		} catch (Exception e) {
			log.error("Could not generate secret key", e);
			throw new IOException("Unable to process key data for " + key.getName(), e);
		}
	}
	
	@Override
	public byte[] generateIvData(SecretKeyResource key) throws IOException {
		
		try {
			return Hex.decodeHex(encryptionProvider.decrypt(key.getIv()).toCharArray());
		} catch (Exception e) {
			log.error("Could not generate iv", e);
			throw new IOException("Unable to process iv data for " + key.getName(), e);
		}
	}

	@Override
	protected void beforeCreateResource(SecretKeyResource resource,
			Map<String, String> properties) throws ResourceCreationException {
		
		SecureRandom rnd = new SecureRandom();
		byte[] rawkey = new byte[32];
		rnd.nextBytes(rawkey);
		
		try {
			resource.setKeydata(encryptionProvider.encrypt(Hex.encodeHexString(rawkey)));
		} catch (Exception e) {
			log.error("Could not encrypt secret key", e);
			throw new ResourceCreationException(RESOURCE_BUNDLE, "error.encryptError", e.getMessage());
		}
		
		byte[] iv = new byte[16];
		rnd.nextBytes(iv);
		
		try {
			resource.setIv(encryptionProvider.encrypt(Hex.encodeHexString(iv)));
		} catch (Exception e) {
			log.error("Could not encrypt iv", e);
			throw new ResourceCreationException(RESOURCE_BUNDLE, "error.encryptError", e.getMessage());
		}
	}

	@Override
	public Class<? extends PermissionType> getPermissionType() {
		return SecretKeyPermission.class;
	}

	@Override
	protected void fireResourceCreationEvent(SecretKeyResource resource) {

	}

	@Override
	protected void fireResourceCreationEvent(SecretKeyResource resource, Throwable t) {

	}

	@Override
	protected void fireResourceUpdateEvent(SecretKeyResource resource) {

	}

	@Override
	protected void fireResourceUpdateEvent(SecretKeyResource resource, Throwable t) {
		
	}

	@Override
	protected void fireResourceDeletionEvent(SecretKeyResource resource) {

	}

	@Override
	protected void fireResourceDeletionEvent(SecretKeyResource resource, Throwable t) {

	}

}
