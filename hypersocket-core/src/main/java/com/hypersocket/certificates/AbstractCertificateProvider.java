package com.hypersocket.certificates;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.cert.CertificateException;
import java.util.Map;

import com.hypersocket.certs.FileFormatException;
import com.hypersocket.certs.InvalidPassphraseException;
import com.hypersocket.certs.X509CertificateUtils;
import com.hypersocket.resource.ResourceCreationException;

public abstract class AbstractCertificateProvider implements CertificateProvider {

	protected KeyPair getKeyPair(CertificateResource resource) throws CertificateException, UnsupportedEncodingException, InvalidPassphraseException, FileFormatException {
		return X509CertificateUtils
				.loadKeyPairFromPEM(new ByteArrayInputStream(resource.getPrivateKey().getBytes("UTF-8")), null);
	}

	protected String getSignatureAlgorithm(Map<String, String> properties) throws ResourceCreationException {
		CertificateType type = CertificateType.valueOf(properties.get("certType"));
		switch (type) {
		case RSA_1024:
			return "SHA1WithRSAEncryption";
		case RSA_2048:
			return "SHA1WithRSAEncryption";
		case DSA_1024:
			return "SHA1WithDSA";
		default:
			throw new ResourceCreationException(CertificateResourceServiceImpl.RESOURCE_BUNDLE,
					"error.unsupportedType");
		}
	}

	protected KeyPair createKeyPair(CertificateResource resource, Map<String, String> properties)
			throws ResourceCreationException, CertificateException {
		CertificateType type = CertificateType.valueOf(properties.get("certType"));

		switch (type) {
		case RSA_1024:
			return X509CertificateUtils.generatePrivateKey("RSA", 1024);
		case RSA_2048:
			return X509CertificateUtils.generatePrivateKey("RSA", 2048);
		case DSA_1024:
			return X509CertificateUtils.generatePrivateKey("DSA", 1024);
		default:
			throw new ResourceCreationException(CertificateResourceServiceImpl.RESOURCE_BUNDLE,
					"error.unsupportedType");
		}
	}
}
