package com.hypersocket.certificates;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.util.Map;

import com.hypersocket.certs.FileFormatException;
import com.hypersocket.certs.InvalidPassphraseException;
import com.hypersocket.certs.X509CertificateUtils;
import com.hypersocket.resource.ResourceCreationException;

public class DefaultCertificateProvider extends AbstractCertificateProvider {
	public final static String RESOURCE_KEY = "default";

	@Override
	public String getResourceKey() {
		return RESOURCE_KEY;
	}

	@Override
	public String getBundle() {
		return CertificateResourceServiceImpl.RESOURCE_BUNDLE;
	}

	@Override
	public void create(CertificateResource resource, Map<String, String> properties)
			throws CertificateException, UnsupportedEncodingException, ResourceCreationException {
		KeyPair pair = createKeyPair(resource, properties);
		String signatureAlgorithm = getSignatureAlgorithm(properties);
		Certificate cert = populateCertificate(properties, pair, signatureAlgorithm);
		resource.setSignatureAlgorithm(signatureAlgorithm);

		ByteArrayOutputStream privateKeyFile = new ByteArrayOutputStream();
		X509CertificateUtils.saveKeyPair(pair, privateKeyFile);

		ByteArrayOutputStream certFile = new ByteArrayOutputStream();
		X509CertificateUtils.saveCertificate(new Certificate[] { cert }, certFile);

		resource.setPrivateKey(new String(privateKeyFile.toByteArray(), "UTF-8"));
		resource.setCertificate(new String(certFile.toByteArray(), "UTF-8"));

	}

	@Override
	public void update(CertificateResource resource, String name, Map<String, String> properties)
			throws CertificateException, UnsupportedEncodingException, InvalidPassphraseException, FileFormatException {
		KeyPair pair = getKeyPair(resource);
		Certificate cert = populateCertificate(properties, pair, resource.getSignatureAlgorithm());

		ByteArrayOutputStream certFile = new ByteArrayOutputStream();
		X509CertificateUtils.saveCertificate(new Certificate[] { cert }, certFile);

		resource.setCertificate(new String(certFile.toByteArray(), "UTF-8"));
		resource.setBundle(null);

	}

	private Certificate populateCertificate(Map<String, String> properties, KeyPair pair, String signatureType) {
		return X509CertificateUtils.generateSelfSignedCertificate(properties.get("commonName"),
				properties.get("organizationalUnit"), properties.get("organization"), properties.get("location"),
				properties.get("state"), properties.get("country"), pair, signatureType);
	}
}
