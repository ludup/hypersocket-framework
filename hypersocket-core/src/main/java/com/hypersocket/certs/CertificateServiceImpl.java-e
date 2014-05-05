/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.certs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hypersocket.auth.AuthenticatedServiceImpl;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;

@Service
public class CertificateServiceImpl extends AuthenticatedServiceImpl implements
		CertificateService {

	static final File INSTALLED_CERT_FILE = new File(System.getProperty("hypersocket.conf", "conf"), "server.crt");
	static final File PRIVATE_KEY_FILE = new File(System.getProperty("hypersocket.conf", "conf"), "server.key");
	static final File CA_CERTS_BUNDLE_FILE = new File(System.getProperty("hypersocket.conf", "conf"), "ca_bundle.crt");
	static final File SELF_SIGNED_CERT_FILE = new File(System.getProperty("hypersocket.conf", "conf"), "self-signed.crt");

	static Logger log = LoggerFactory.getLogger(CertificateServiceImpl.class);

	@Autowired
	PermissionService permissionService;

	@PostConstruct
	private void postConstruct() {

		PermissionCategory cat = permissionService.registerPermissionCategory(
				RESOURCE_BUNDLE, "category.certificate");

		for (CertificatePermission p : CertificatePermission.values()) {
			permissionService.registerPermission(p.getResourceKey(), cat);
		}
	}

	@Override
	public KeyStore getDefaultCertificate() throws AccessDeniedException,
			CertificateException {

		try {
			if (!hasPrivateKey()) {
				generatePrivateKey(Integer.parseInt(System.getProperty(
						"hypersocket.defaultKeySize", "2048")), true);
			}

			if (hasInstalledCertificate()) {

				try {
					return loadPEMCertificate(INSTALLED_CERT_FILE, CA_CERTS_BUNDLE_FILE, null,
							"changeit".toCharArray());

				} catch (MismatchedCertificateException ex) {
					if (log.isErrorEnabled()) {
						log.error("Installed certificate does not match private key");
					}
				}
			}

			if (hasSelfSignedCertificate()) {
				try {
					return loadPEMCertificate(SELF_SIGNED_CERT_FILE, null, null,
							"changeit".toCharArray());

				} catch (MismatchedCertificateException ex) {
					if (log.isErrorEnabled()) {
						log.error("Self-signed certificate does not match private key");
					}
				}

			}

			String hostname = "localhost";
			try {
				hostname = InetAddress.getLocalHost().getHostName();
			} catch (Throwable t) {
			}

			generateSelfSignedCertifcate(hostname, true);

			return loadPEMCertificate(SELF_SIGNED_CERT_FILE, null, null,
					"changeit".toCharArray());
		} catch (Exception e) {
			if (log.isErrorEnabled()) {
				log.error("Failed to load PEM certificate", e);
			}
		}

		try {
			KeyPair pair = generatePrivateKey(Integer.parseInt(System
					.getProperty("hypersocket.defaultKeySize", "2048")), false);
			X509Certificate cert = generateSelfSignedCertifcate("localhost", false);

			return X509CertificateUtils.createKeystore(pair, new X509Certificate[] {cert},
					"server", "changeit".toCharArray());
		} catch (Exception e) {
			throw new CertificateException(
					"Failed in last chance attempt to generate a key & certificate",
					e);
		}

	}

	public boolean hasSelfSignedCertificate() {
		return SELF_SIGNED_CERT_FILE.exists();
	}

	@Override
	public boolean hasInstalledCertificate() throws AccessDeniedException {

		assertAnyPermission(CertificatePermission.CERTIFICATE_ADMINISTRATION);

		return INSTALLED_CERT_FILE.exists();
	}

	protected KeyStore loadPEMCertificate(File certFile,
			File caFile,
			char[] keyPassphrase, char[] keystorePassphrase)
			throws CertificateException, MismatchedCertificateException {

		try {
			if(caFile!=null && caFile.exists()) {
				return X509CertificateUtils.createKeystore(
						X509CertificateUtils.loadKeyPairFromPEM(new FileInputStream(PRIVATE_KEY_FILE), keyPassphrase),
						X509CertificateUtils.validateChain(
								X509CertificateUtils.loadCertificateChainFromPEM(new FileInputStream(caFile)), 
								X509CertificateUtils.loadCertificateFromPEM(new FileInputStream(certFile))), 
						"hypersocket", keystorePassphrase);
			} else {
				return X509CertificateUtils.createKeystore(
						X509CertificateUtils.loadKeyPairFromPEM(new FileInputStream(PRIVATE_KEY_FILE), keyPassphrase),
						new X509Certificate[] {
								X509CertificateUtils.loadCertificateFromPEM(new FileInputStream(certFile))}, 
						"hypersocket", keystorePassphrase);
			}
		} catch (MismatchedCertificateException ex) {
			throw ex;
		} catch (Exception e) {
			throw new CertificateException(
					"Failed to load key/certificate files", e);
		}

	}

	public boolean hasPrivateKey() {
		return PRIVATE_KEY_FILE.exists();
	}

	public KeyPair getPrivateKey() throws CertificateException {
		try {
			return X509CertificateUtils.loadKeyPairFromPEM(
					new FileInputStream(PRIVATE_KEY_FILE), new char[] {});
		} catch (Exception e) {
			throw new CertificateException("Failed to load private key", e);
		}
	}

	public KeyPair generatePrivateKey(int bits, boolean save)
			throws CertificateException {

		if (log.isInfoEnabled()) {
			log.info("Generating private key");
		}
		try {
			KeyPair pair = X509CertificateUtils.generatePrivateKey(bits);
			if (save) {
				X509CertificateUtils.saveKeyPair(pair,
						new FileOutputStream(PRIVATE_KEY_FILE));
			}
			if (log.isInfoEnabled()) {
				log.info("Completed private key generation");
			}
			return pair;
		} catch (Exception e) {
			throw new CertificateException("Failed to create private key", e);
		} finally {

		}
	}

	public X509Certificate generateSelfSignedCertifcate(String hostname,
			boolean save) throws CertificateException {

		if(log.isInfoEnabled()) {
			log.info("Generating a self-sgined certificate");
		}
		try {
			X509Certificate cert = X509CertificateUtils
					.generateSelfSignedCertificate(hostname, getPrivateKey());
			if (save) {
				X509CertificateUtils.saveCertificate(new X509Certificate[] {cert},
						new FileOutputStream(SELF_SIGNED_CERT_FILE));
			}
			return cert;

		} catch (FileNotFoundException e) {
			throw new CertificateException(
					"Failed to generate self-signed certificate");
		}
	}

	@Override
	public boolean resetPrivateKey() throws AccessDeniedException {

		assertAnyPermission(CertificatePermission.CERTIFICATE_ADMINISTRATION);

		boolean success = false;

		if (PRIVATE_KEY_FILE.exists()) {
			success = PRIVATE_KEY_FILE.delete();
			if (!success) {
				if (log.isErrorEnabled()) {
					log.error("Failed to delete " + PRIVATE_KEY_FILE.getName());
				}
				return false;
			}
		}

		if (SELF_SIGNED_CERT_FILE.exists()) {
			success = SELF_SIGNED_CERT_FILE.delete();
			if (!success) {
				if (log.isErrorEnabled()) {
					log.error("Failed to delete " + SELF_SIGNED_CERT_FILE.getName());
				}
				return false;
			}
		}

		if (CA_CERTS_BUNDLE_FILE.exists()) {
			success = CA_CERTS_BUNDLE_FILE.delete();
			if (!success) {
				if (log.isErrorEnabled()) {
					log.error("Failed to delete " + CA_CERTS_BUNDLE_FILE.getName());
				}
				return false;
			}
		}
		
		if (INSTALLED_CERT_FILE.exists()) {
			success = INSTALLED_CERT_FILE.delete();
			if (!success) {
				if (log.isErrorEnabled()) {
					log.error("Failed to delete " + INSTALLED_CERT_FILE.getName());
				}
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean updatePrivateKey(MultipartFile file, String passphrase, MultipartFile cert, MultipartFile bundle)
			throws AccessDeniedException, InvalidPassphraseException,
			FileFormatException {

		assertAnyPermission(CertificatePermission.CERTIFICATE_ADMINISTRATION);

		try {

			KeyPair pair = X509CertificateUtils.loadKeyPairFromPEM(
					file.getInputStream(), passphrase.toCharArray());

			X509CertificateUtils.saveKeyPair(pair, new FileOutputStream(
					PRIVATE_KEY_FILE));

			CA_CERTS_BUNDLE_FILE.delete();
			INSTALLED_CERT_FILE.delete();
			SELF_SIGNED_CERT_FILE.delete();
			
			updateCertificate(file, bundle);
			
			return true;
		} catch (InvalidPassphraseException ex) {
			if (log.isInfoEnabled()) {
				log.info("Failed to update private key, invalid passphrase detected.");
			}
			throw ex;
		} catch (FileFormatException ex) {
			if (log.isInfoEnabled()) {
				log.info("Failed to parse private key, invalid format.");
			}
			throw ex;
		} catch (Exception ex) {
			if (log.isErrorEnabled()) {
				log.error(
						"Failed to update private key from MultipartFile "
								+ file.getOriginalFilename() + " size="
								+ file.getSize(), ex);
			}
			return false;
		}

	}

	@Override
	public boolean hasWorkingCertificate() throws AccessDeniedException {

		try {

			if (log.isInfoEnabled()) {
				log.info("Checking for a working key/certificate using private key and installed certificate");
			}
			loadPEMCertificate(INSTALLED_CERT_FILE, CA_CERTS_BUNDLE_FILE, null, "changeit".toCharArray());
			return true;
		} catch (Exception ex) {
			if(log.isErrorEnabled()) {
				log.error("Could not parse installed certificate", ex);
			}
		}
		
		try {
			if (log.isInfoEnabled()) {
				log.info("Checking for a working key/certificate using private key and self-signed certificate");
			}
			loadPEMCertificate(SELF_SIGNED_CERT_FILE, null, null, "changeit".toCharArray());
			return true;
		} catch (Exception ex2) {
			if (log.isInfoEnabled()) {
				log.info("There are no working certificates installed", ex2);
			}
			return false;
		}
		
	}

	@Override
	public boolean updateCertificate(MultipartFile file, MultipartFile bundle) throws AccessDeniedException, FileFormatException, MismatchedCertificateException, CertificateException, IOException, InvalidPassphraseException {
		
		assertAnyPermission(CertificatePermission.CERTIFICATE_ADMINISTRATION);
	
		X509Certificate cert = X509CertificateUtils.loadCertificateFromPEM(file.getInputStream());
		
		X509Certificate[] ca = X509CertificateUtils.loadCertificateChainFromPEM(bundle.getInputStream());
		
		X509CertificateUtils.validateChain(ca, cert);
		
		KeyPair pair = X509CertificateUtils.loadKeyPairFromPEM(new FileInputStream(PRIVATE_KEY_FILE), null);
		
		if(!pair.getPublic().equals(cert.getPublicKey())) {
			throw new MismatchedCertificateException();
		}
		
		X509CertificateUtils.saveCertificate(new X509Certificate[] {cert}, new FileOutputStream(INSTALLED_CERT_FILE));
		X509CertificateUtils.saveCertificate(ca, new FileOutputStream(CA_CERTS_BUNDLE_FILE));

		SELF_SIGNED_CERT_FILE.delete();
		
		return true;
 
	}

	@Override
	public String generateCSR(String cn, String ou, String o, String l, String s,
			String c) throws UnsupportedEncodingException, Exception {
		
		KeyPair pair = getPrivateKey();
		return new String(X509CertificateUtils.generatePKCS10(pair.getPrivate(), pair.getPublic(), cn, ou, o, l, s, c), "UTF-8");
		
	}


}
