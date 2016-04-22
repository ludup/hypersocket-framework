package com.hypersocket.secret;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.crypto.Cipher;

import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.util.Arrays;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.encrypt.EncryptionProvider;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.rsa.RsaEncryptionProvider;
import com.hypersocket.session.SessionService;

@Service
public class SecretKeyServiceImpl extends
		AbstractResourceServiceImpl<SecretKeyResource> implements SecretKeyService {

	public static final String RESOURCE_BUNDLE = "SecretKeyService";
	
	static Logger log = LoggerFactory.getLogger(SecretKeyServiceImpl.class);
	
	@Autowired
	SecretKeyRepository repository;
	
	@Autowired
	SessionService sessionService; 
	
	@Autowired
	RealmService realmService; 
	
	
	EncryptionProvider encryptionProvider;
	
	public SecretKeyServiceImpl() {
		super("secretKeyResource");
	}

	@PostConstruct
	private void postConstruct() throws Exception {
		if(encryptionProvider==null) {
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
	
	protected Class<SecretKeyResource> getResourceClass() {
		return SecretKeyResource.class;
	}
	
	@Override
	public SecretKeyResource createSecretKey(Realm realm) throws ResourceCreationException, AccessDeniedException {

		String name = UUID.randomUUID().toString();
		return createSecretKey(name, realm);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public SecretKeyResource createSecretKey(String name, Realm realm) throws ResourceCreationException, AccessDeniedException {
		
		
		try {
			SecretKeyResource key = new SecretKeyResource();
			key.setName(name);
			key.setRealm(realm);
			key.setResourceCategory("secretKey");
			key.setKeylength(Math.min(Cipher.getMaxAllowedKeyLength("AES"), 256));
			
			SecureRandom rnd = new SecureRandom();
			byte[] rawkey = new byte[32];
			rnd.nextBytes(rawkey);
			
			try {
				key.setKeydata(encryptionProvider.encrypt(Hex.encodeHexString(rawkey)));
			} catch (Exception e) {
				log.error("Could not encrypt secret key", e);
				throw new ResourceCreationException(RESOURCE_BUNDLE, "error.encryptError", e.getMessage());
			}
			
			byte[] iv = new byte[16];
			rnd.nextBytes(iv);
			
			try {
				key.setIv(encryptionProvider.encrypt(Hex.encodeHexString(iv)));
			} catch (Exception e) {
				log.error("Could not encrypt iv", e);
				throw new ResourceCreationException(RESOURCE_BUNDLE, "error.encryptError", e.getMessage());
			}
			
			repository.saveResource(key, new HashMap<String,String>());
			
			return key;
		} catch (NoSuchAlgorithmException e) {
			throw new ResourceCreationException(RESOURCE_BUNDLE, "error.aesNotSupported");
		}
		
	}

	
	@Override
	public byte[] generateSecreyKeyData(SecretKeyResource key) throws IOException {
		
		try {
			return Arrays.copyOf(Hex.decodeHex(encryptionProvider.decrypt(key.getKeydata()).toCharArray()), key.getKeylength() / 8);
		} catch (Exception e) {
			log.error("Could not generate secret key", e);
			throw new IOException("Unable to process key data for " + key.getName(), e);
		}
	}
	
	@Override
	public byte[] generateIvData(SecretKeyResource key) throws IOException {
		
		try {
			return Arrays.copyOf(Hex.decodeHex(encryptionProvider.decrypt(key.getIv()).toCharArray()), 16);
		} catch (Exception e) {
			log.error("Could not generate iv", e);
			throw new IOException("Unable to process iv data for " + key.getName(), e);
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

	@Override
	public SecretKeyResource getSecretKey(String reference, Realm realm) throws ResourceNotFoundException, ResourceCreationException, AccessDeniedException {
		SecretKeyResource key = repository.getResourceByName(reference, realm);
		return key;
	}

	@Override
	public void setEncryptorProvider(EncryptionProvider encryptionProvider) {
		if(log.isInfoEnabled()) {
			log.info("Installed " + encryptionProvider.getName() + " encryption provider");
		}
		this.encryptionProvider = encryptionProvider;
	}
	
	@Override
	public EncryptionProvider getEncryptorProvider() {
		return encryptionProvider;
	}

}
