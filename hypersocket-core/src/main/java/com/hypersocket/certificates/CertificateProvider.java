package com.hypersocket.certificates;

import java.io.UnsupportedEncodingException;
import java.security.cert.CertificateException;
import java.util.Map;

import com.hypersocket.certs.FileFormatException;
import com.hypersocket.certs.InvalidPassphraseException;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceException;

public interface CertificateProvider {
	
	boolean isRequireCertificateDetails();

	String getResourceKey();
	
	String getBundle();

	void create(CertificateResource resource, Map<String, String> properties) throws CertificateException, UnsupportedEncodingException, ResourceException, AccessDeniedException;

	boolean update(CertificateResource resource, String name, Map<String, String> properties) throws CertificateException, UnsupportedEncodingException, InvalidPassphraseException, FileFormatException, ResourceException, AccessDeniedException;

}
