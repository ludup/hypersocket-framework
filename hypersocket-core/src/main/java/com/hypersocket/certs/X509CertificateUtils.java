/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.certs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.security.auth.x500.X500Principal;

import org.apache.commons.io.IOUtils;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.asn1.x500.X500NameBuilder;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.ExtensionsGenerator;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.openssl.PEMEncryptedKeyPair;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JceOpenSSLPKCS8DecryptorProviderBuilder;
import org.bouncycastle.openssl.jcajce.JcePEMDecryptorProviderBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.pkcs.PKCS10CertificationRequest;
import org.bouncycastle.pkcs.PKCS10CertificationRequestBuilder;
import org.bouncycastle.pkcs.PKCS8EncryptedPrivateKeyInfo;
import org.bouncycastle.pkcs.jcajce.JcaPKCS10CertificationRequestBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class X509CertificateUtils {

	static Logger log = LoggerFactory.getLogger(X509CertificateUtils.class);

	private static final String BC = org.bouncycastle.jce.provider.BouncyCastleProvider.PROVIDER_NAME;

	public static KeyStore loadKeystoreFromPEM(InputStream keyfile,
			InputStream certfile, char[] keyPassphrase,
			char[] keystorePassphrase, String keystoreAlias)
			throws CertificateException, IOException, NoSuchAlgorithmException,
			KeyStoreException, InvalidPassphraseException, FileFormatException,
			MismatchedCertificateException {
		KeyPair pair = loadKeyPairFromPEM(keyfile, keyPassphrase);
		X509Certificate cert = loadCertificateFromPEM(certfile);

		return createKeystore(pair, new X509Certificate[] { cert },
				keystoreAlias, keystorePassphrase);

	}

	public static KeyStore loadKeystoreFromPEM(InputStream keyfile,
			InputStream certfile, char[] keyPassphrase,
			char[] keystorePassphrase) throws CertificateException,
			IOException, NoSuchAlgorithmException, KeyStoreException,
			InvalidPassphraseException, FileFormatException,
			MismatchedCertificateException {
		return loadKeystoreFromPEM(keyfile, certfile, keyPassphrase,
				keystorePassphrase, "importedPEM");

	}

	public static X509Certificate[] validateChain(
			Certificate[] caRootAndInters, X509Certificate cert)
			throws CertificateException {

		if (log.isInfoEnabled()) {
			log.info("Validating certificate against certificate chain");
		}

		List<X509Certificate> certs = new ArrayList<X509Certificate>();

		cert.checkValidity();

		certs.add(cert);
		X509Certificate lastCert = cert;

		if (log.isInfoEnabled()) {
			log.info("Certificate Subject: " + cert.getSubjectDN());
			log.info("Issued By: " + cert.getIssuerDN());
			log.info("Validating chain");
		}

		try {
			for (Certificate cc : caRootAndInters) {

				X509Certificate c = (X509Certificate) cc;
				if (log.isInfoEnabled()) {
					log.info("Checking validity of certificate "
							+ c.getSubjectDN());
					log.info("Issued By: " + c.getIssuerDN());
				}

				c.checkValidity();

				if (log.isInfoEnabled()) {
					log.info("Certificate is valid, verifying certificate is signed by next certificate in chain");
				}

				lastCert.verify(c.getPublicKey());

				if (log.isInfoEnabled()) {
					log.info("Certificate has been verified against next certificate in chain");
				}
				certs.add(c);
				lastCert = c;
			}

			return certs.toArray(new X509Certificate[0]);
		} catch (CertificateExpiredException e) {
			throw e;
		} catch (CertificateNotYetValidException e) {
			throw e;
		} catch (Exception e) {
			throw new CertificateException(e);
		}

	}

	public static X509Certificate[] loadCertificateChainFromPEM(
			InputStream certfile) throws IOException, CertificateException,
			FileFormatException {

		List<X509Certificate> certs = new ArrayList<X509Certificate>();

		PEMParser parser = new PEMParser(new InputStreamReader(certfile));

		try {

			Object obj = null;
			while ((obj = parser.readObject()) != null) {

				if (obj instanceof X509CertificateHolder) {
					certs.add(new JcaX509CertificateConverter().setProvider(
							"BC").getCertificate((X509CertificateHolder) obj));
				} else {
					throw new FileFormatException(
							"Failed to read X509Certificate from InputStream provided");
				}
			}

			return certs.toArray(new X509Certificate[0]);

		} finally {
			IOUtils.closeQuietly(certfile);
			parser.close();
		}
	}

	public static X509Certificate loadCertificateFromPEM(InputStream certfile)
			throws IOException, CertificateException, FileFormatException {
		PEMParser parser = new PEMParser(new InputStreamReader(certfile));

		try {

			Object obj = parser.readObject();

			if (obj instanceof X509CertificateHolder) {
				return new JcaX509CertificateConverter().setProvider("BC")
						.getCertificate((X509CertificateHolder) obj);
			} else {
				throw new FileFormatException(
						"Failed to read X509Certificate from InputStream provided");
			}

		} finally {
			IOUtils.closeQuietly(certfile);
			parser.close();
		}
	}

	public static void saveKeyPair(KeyPair pair, OutputStream keyfile)
			throws CertificateException {

		JcaPEMWriter writer = new JcaPEMWriter(new OutputStreamWriter(keyfile));
		try {
			writer.writeObject(pair.getPrivate());
			writer.flush();
			writer.close();
		} catch (IOException e) {
			throw new CertificateException("Failed to save key pair", e);
		}
	}

	public static void saveCertificate(Certificate[] certs,
			OutputStream certfile) throws CertificateException {
		JcaPEMWriter writer = new JcaPEMWriter(new OutputStreamWriter(certfile));

		try {
			for (Certificate c : certs) {
				writer.writeObject(c);
			}
			writer.flush();
			writer.close();
		} catch (IOException e) {
			throw new CertificateException("Failed to save certificate", e);
		}

	}

	public static KeyStore loadKeyStoreFromPFX(InputStream pfxfile,
			char[] passphrase) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException,
			NoSuchProviderException, UnrecoverableKeyException {

		try {
			KeyStore keystore = KeyStore.getInstance("PKCS12", "BC");
			keystore.load(pfxfile, passphrase);
			return keystore;
		} finally {
			IOUtils.closeQuietly(pfxfile);
		}
	}

	public static KeyStore loadKeyStoreFromJKS(InputStream jksFile,
			char[] passphrase) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException,
			NoSuchProviderException, UnrecoverableKeyException {

		try {
			KeyStore keystore = KeyStore.getInstance("JKS");
			keystore.load(jksFile, passphrase);
			return keystore;
		} finally {
			IOUtils.closeQuietly(jksFile);
		}
	}

	public static KeyPair loadKeyPairFromPEM(InputStream keyfile,
			char[] passphrase) throws InvalidPassphraseException,
			CertificateException, FileFormatException {

		PEMParser parser = new PEMParser(new InputStreamReader(keyfile));
		JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
		try {

			Object privatekey = parser.readObject();

			if (privatekey instanceof PEMEncryptedKeyPair) {
				try {
					privatekey = ((PEMEncryptedKeyPair) privatekey)
							.decryptKeyPair(new JcePEMDecryptorProviderBuilder()
									.build(passphrase));
				} catch (Exception e) {
					throw new InvalidPassphraseException(e);
				}
			} else if (privatekey instanceof PKCS8EncryptedPrivateKeyInfo) {
				try {
					privatekey = converter
							.getPrivateKey(((PKCS8EncryptedPrivateKeyInfo) privatekey)
									.decryptPrivateKeyInfo(new JceOpenSSLPKCS8DecryptorProviderBuilder()
											.build(passphrase)));
				} catch (Exception e) {
					throw new InvalidPassphraseException(e);
				}
			}

			if (privatekey instanceof PEMKeyPair) {
				return loadKeyPair((PEMKeyPair) privatekey);
			} else if (privatekey instanceof RSAPrivateCrtKey) {
				return loadKeyPair((RSAPrivateCrtKey) privatekey);
			} else if(privatekey instanceof PrivateKeyInfo){
				PrivateKeyInfo i = (PrivateKeyInfo) privatekey;
				PrivateKey prv = converter.getPrivateKey(i);
				if(prv instanceof RSAPrivateCrtKey) {
					return loadKeyPair((RSAPrivateCrtKey)prv);
				} else {
					throw new FileFormatException("Unsupported private key type");
				}
			} else {
				throw new FileFormatException(
						"The file doesn't seem to have any supported key types obj="
								+ privatekey);
			}

		} catch (IOException ex) {
			throw new CertificateException("Failed to read from key file", ex);
		} finally {
			IOUtils.closeQuietly(keyfile);
			try {
				parser.close();
			} catch (IOException e) {
			}
		}
	}

	private static KeyPair loadKeyPair(RSAPrivateCrtKey privatekey)
			throws CertificateException {
		try {
			RSAPrivateCrtKey k = (RSAPrivateCrtKey) privatekey;

			KeyFactory keyMaker = KeyFactory.getInstance("RSA");
			RSAPublicKeySpec pubKeySpec = new RSAPublicKeySpec(k.getModulus(),
					k.getPublicExponent());

			RSAPublicKey pubKey = (RSAPublicKey) keyMaker
					.generatePublic(pubKeySpec);

			return new KeyPair(pubKey, k);
		} catch (Exception e) {
			throw new CertificateException(
					"Failed to convert RSAPrivateCrtKey into JCE KeyPair", e);
		}
	}

	private static KeyPair loadKeyPair(PEMKeyPair privatekey)
			throws CertificateException {
		try {			
			return new JcaPEMKeyConverter().getKeyPair(privatekey);
		} catch (Exception e) {
			throw new CertificateException(
					"Failed to convert PEMKeyPair into JCE KeyPair", e);
		}
	}

	public static KeyPair generatePrivateKey(String algorithm, int bits)
			throws CertificateException {
		try {
			KeyPairGenerator kpGen = KeyPairGenerator.getInstance(algorithm, "BC");
			kpGen.initialize(bits, new SecureRandom());
			return kpGen.generateKeyPair();
		} catch (Throwable t) {
			throw new CertificateException("Failed to generate private key", t);
		}
	}
	
	static GeneralNames generateNames(String cn, String[] san) {
		List<GeneralName> names = new ArrayList<>();
		GeneralName altName = new GeneralName(GeneralName.dNSName, cn);
		names.add(altName);
		for(String s : san) {
			int idx = s.indexOf(':');
			if(idx == -1)
				log.warn("Invalid format SAN. " + s);
			else {
				names.add(new GeneralName(toSANType(s.substring(0, idx)), s.substring(idx + 1)));
			}
		}
		return new GeneralNames(names.toArray(new GeneralName[0]));
	}

	static int toSANType(String name) {
		// EMAIL, DNS, URI, IP, OID
		if(name.equalsIgnoreCase("EMAIL")) {
			return GeneralName.rfc822Name;
		}
		else if(name.equalsIgnoreCase("DNS")) {
			return GeneralName.dNSName;
		}
		else if(name.equalsIgnoreCase("URI")) {
			return GeneralName.uniformResourceIdentifier;
		}
		else if(name.equalsIgnoreCase("IP")) {
			return GeneralName.iPAddress;
		}
		else if(name.equalsIgnoreCase("OID")) {
			return GeneralName.registeredID;
		}
		else if(name.equalsIgnoreCase("OTHER")) {
			return GeneralName.otherName;
		}
		else if(name.equalsIgnoreCase("X400")) {
			return GeneralName.x400Address;
		}
		else if(name.equalsIgnoreCase("EDI")) {
			return GeneralName.ediPartyName;
		}
		else {
			return GeneralName.otherName;
		}
	}

	public static X509Certificate generateSelfSignedCertificate(
			String cn, String ou, String o, String l, String s, String c, KeyPair pair, String signatureType, String[] san) {
		try {
			// Generate self-signed certificate
			X500NameBuilder builder = new X500NameBuilder(BCStyle.INSTANCE);
			builder.addRDN(BCStyle.OU, ou);
			builder.addRDN(BCStyle.O, o);
			builder.addRDN(BCStyle.L, l);
			builder.addRDN(BCStyle.ST, s);
			builder.addRDN(BCStyle.CN, cn);
			
			Date notBefore = new Date(System.currentTimeMillis() - 1000L * 60
					* 60 * 24 * 30);
			Date notAfter = new Date(System.currentTimeMillis()
					+ (1000L * 60 * 60 * 24 * 365 * 10));

			BigInteger serial = BigInteger.valueOf(System.currentTimeMillis());

			X509v3CertificateBuilder certGen = new JcaX509v3CertificateBuilder(
					builder.build(), serial, notBefore, notAfter,
					builder.build(), pair.getPublic());
			
			certGen.addExtension(Extension.subjectAlternativeName, false, generateNames(cn, san)); 
			
			ContentSigner sigGen = new JcaContentSignerBuilder(
					signatureType).setProvider(BC).build(
					pair.getPrivate());
			X509Certificate cert = new JcaX509CertificateConverter()
					.setProvider(BC).getCertificate(certGen.build(sigGen));
			cert.checkValidity(new Date());
			cert.verify(cert.getPublicKey());

			return cert;

		} catch (Throwable t) {
			throw new RuntimeException(
					"Failed to generate self-signed certificate!", t);
		}
	}

	public static KeyStore createKeystore(KeyPair pair, X509Certificate[] cert,
			String alias, char[] keystorePassphrase) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException,
			MismatchedCertificateException {
		return createKeystore(pair, cert, alias, keystorePassphrase, "JKS");
	}
	
	public static KeyStore createPKCS12Keystore(KeyPair pair, X509Certificate[] cert,
			String alias, char[] keystorePassphrase) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException,
			MismatchedCertificateException {
		return createKeystore(pair, cert, alias, keystorePassphrase, "PKCS12");
	}
	
	public static KeyStore createKeystore(KeyPair pair, X509Certificate[] cert,
			String alias, char[] keystorePassphrase, String keystoreType) throws KeyStoreException,
			NoSuchAlgorithmException, CertificateException, IOException,
			MismatchedCertificateException {
		KeyStore store = KeyStore.getInstance(keystoreType);
		store.load(null);

		if (!pair.getPublic().equals(cert[0].getPublicKey())) {
			throw new MismatchedCertificateException();
		}
		store.setKeyEntry(alias, pair.getPrivate(), keystorePassphrase, cert);

		return store;
	}

	public static byte[] generatePKCS10(PrivateKey privateKey,
			PublicKey publicKey, String CN, String OU, String O, String L,
			String S, String C, String[] SAN) throws Exception {

		JcaContentSignerBuilder csb = new JcaContentSignerBuilder("SHA1withRSA");
		ContentSigner cs = csb.build(privateKey);

		X500NameBuilder nameBuilder = new X500NameBuilder(BCStyle.INSTANCE);
		nameBuilder.addRDN(BCStyle.OU, OU);
		nameBuilder.addRDN(BCStyle.O, O);
		nameBuilder.addRDN(BCStyle.L, L);
		nameBuilder.addRDN(BCStyle.C, C);
		nameBuilder.addRDN(BCStyle.ST, S);
		nameBuilder.addRDN(BCStyle.CN, CN);
		
		X500Principal principal = new X500Principal(nameBuilder.build().getEncoded());
		
		PKCS10CertificationRequestBuilder builder = new JcaPKCS10CertificationRequestBuilder(
				principal, publicKey);

		ExtensionsGenerator extGen = new ExtensionsGenerator();
        extGen.addExtension(Extension.subjectAlternativeName, false, generateNames(CN, SAN));
		builder.addAttribute(PKCSObjectIdentifiers.pkcs_9_at_extensionRequest, extGen.generate());
		
		PKCS10CertificationRequest req = builder.build(cs);

		ByteArrayOutputStream bout = new ByteArrayOutputStream();
		JcaPEMWriter p = null;
		try {
			p = new JcaPEMWriter(new OutputStreamWriter(bout));
			p.writeObject(req);
		} finally {
			if (p != null)
				p.close();
		}
		return bout.toByteArray();

	}

}
