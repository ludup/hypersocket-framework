package com.hypersocket.certificates;

import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.util.Map;

import com.hypersocket.certs.FileFormatException;
import com.hypersocket.certs.InvalidPassphraseException;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;

public interface CertificateProvider {
	
	boolean isRequireCertificateDetails();

	String getResourceKey();
	
	String getBundle();

	void create(CertificateResource resource, Map<String, String> properties) throws CertificateException, UnsupportedEncodingException, ResourceCreationException;

	void update(CertificateResource resource, String name, Map<String, String> properties) throws CertificateException, UnsupportedEncodingException, InvalidPassphraseException, FileFormatException, ResourceChangeException;

}
