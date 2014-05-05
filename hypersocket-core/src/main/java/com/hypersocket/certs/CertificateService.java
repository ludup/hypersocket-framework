/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.certs;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;

import org.springframework.web.multipart.MultipartFile;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.permissions.AccessDeniedException;

public interface CertificateService extends AuthenticatedService {

	static final String RESOURCE_BUNDLE = "CertificateService";
	
	KeyStore getDefaultCertificate() throws AccessDeniedException, CertificateException;

	boolean hasInstalledCertificate() throws AccessDeniedException;

	boolean hasWorkingCertificate() throws AccessDeniedException; 
	
	boolean resetPrivateKey() throws AccessDeniedException;

	void updatePrivateKey(MultipartFile file, String passphrase, MultipartFile cert, MultipartFile bundle) throws AccessDeniedException, InvalidPassphraseException, FileFormatException, CertificateException, IOException, MismatchedCertificateException;

	void updateCertificate(MultipartFile file, MultipartFile bundle) throws AccessDeniedException, FileFormatException, CertificateExpiredException, CertificateNotYetValidException, MismatchedCertificateException, CertificateException, IOException, InvalidPassphraseException;

	String generateCSR(String cn, String ou, String o, String l, String s, String c) throws UnsupportedEncodingException, Exception;

	void updatePrivateKey(MultipartFile pfx, String passphrase)
			throws CertificateException, UnrecoverableKeyException,
			KeyStoreException, NoSuchAlgorithmException,
			NoSuchProviderException, IOException,
			MismatchedCertificateException;


}
