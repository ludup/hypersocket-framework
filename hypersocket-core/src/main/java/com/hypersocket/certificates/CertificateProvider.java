package com.hypersocket.certificates;

import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.util.Map;

import com.hypersocket.certs.FileFormatException;
import com.hypersocket.certs.InvalidPassphraseException;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.util.Pair;

public interface CertificateProvider {
	
	boolean isRequireCertificateDetails();

	String getResourceKey();
	
	String getBundle();

	void create(CertificateResource resource, Map<String, String> properties) throws CertificateException, UnsupportedEncodingException, ResourceException, AccessDeniedException;

	boolean update(CertificateResource resource, String name, Map<String, String> properties) throws CertificateException, UnsupportedEncodingException, InvalidPassphraseException, FileFormatException, ResourceException, AccessDeniedException;

	/**
	 * 
	 * Lets provider decide certificate can be created in current request flow or requires some pre condition.
	 * 
	 * @param resource
	 * @param realm
	 * @param properties
	 * @param isCreate
	 * @return
	 */
	default boolean isDeferredCertificateCreation(CertificateResource resource, Realm realm, Map<String, String> properties, boolean isCreate) {
		return false;
	}
	
	/**
	 * Send a pair first bundle second key.
	 * 
	 * @return
	 */
	default Pair<String> deferredCertificateCreationMessageInfo() {
		return new Pair<String>("", "");
	}
	
	
	/**
	 * Providers at times might have pending action before a certificate is ready for production use.
	 * For e.g. Lets encrypt wild card certificates need to complete DNS verification using action framework.
	 * When these certificates are used api should check they are ready for production use or not.
	 * 
	 * @return Certificate is ready for production or not. true implies ready.
	 */
	default boolean isCertificateProductionReady(CertificateResource resource) {
		return true;
	}
}
