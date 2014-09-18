package com.hypersocket.certificates;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hypersocket.certificates.events.CertificateResourceCreatedEvent;
import com.hypersocket.certificates.events.CertificateResourceDeletedEvent;
import com.hypersocket.certificates.events.CertificateResourceUpdatedEvent;
import com.hypersocket.certs.FileFormatException;
import com.hypersocket.certs.InvalidPassphraseException;
import com.hypersocket.certs.MismatchedCertificateException;
import com.hypersocket.certs.X509CertificateUtils;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceNotFoundException;

@Service
public class CertificateResourceServiceImpl extends
		AbstractResourceServiceImpl<CertificateResource> implements
		CertificateResourceService {

	static Logger log = LoggerFactory
			.getLogger(CertificateResourceServiceImpl.class);

	public static final String RESOURCE_BUNDLE = "CertificateResourceService";

	@Autowired
	CertificateResourceRepository repository;

	@Autowired
	I18NService i18nService;

	@Autowired
	PermissionService permissionService;

	@Autowired
	RealmService realmService;

	@Autowired
	EventService eventService;

	@PostConstruct
	private void postConstruct() {

		i18nService.registerBundle(RESOURCE_BUNDLE);

		PermissionCategory cat = permissionService.registerPermissionCategory(
				RESOURCE_BUNDLE, "category.certificates");

		for (CertificateResourcePermission p : CertificateResourcePermission
				.values()) {
			permissionService.registerPermission(p, cat);
		}

		repository.loadPropertyTemplates("certificateResourceTemplate.xml");

		/**
		 * Register the events. All events have to be registerd so the system
		 * knows about them.
		 */
		eventService.registerEvent(CertificateResourceCreatedEvent.class,
				RESOURCE_BUNDLE, this);
		eventService.registerEvent(CertificateResourceUpdatedEvent.class,
				RESOURCE_BUNDLE, this);
		eventService.registerEvent(CertificateResourceDeletedEvent.class,
				RESOURCE_BUNDLE, this);

	}

	@Override
	protected AbstractResourceRepository<CertificateResource> getRepository() {
		return repository;
	}

	@Override
	protected String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public Class<CertificateResourcePermission> getPermissionType() {
		return CertificateResourcePermission.class;
	}

	@Override
	protected void fireResourceCreationEvent(CertificateResource resource) {

		/**
		 * Prevent event from firing during initial default certificate creation
		 */
		if (!getCurrentPrincipal().isSystem()) {
			eventService.publishEvent(new CertificateResourceCreatedEvent(this,
					getCurrentSession(), resource));
		}
	}

	@Override
	protected void fireResourceCreationEvent(CertificateResource resource,
			Throwable t) {

		/**
		 * Prevent event from firing during initial default certificate creation
		 */
		if (!getCurrentPrincipal().isSystem()) {
			eventService.publishEvent(new CertificateResourceCreatedEvent(this,
					resource, t, getCurrentSession()));
		}
	}

	@Override
	protected void fireResourceUpdateEvent(CertificateResource resource) {
		eventService.publishEvent(new CertificateResourceUpdatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceUpdateEvent(CertificateResource resource,
			Throwable t) {
		eventService.publishEvent(new CertificateResourceUpdatedEvent(this,
				resource, t, getCurrentSession()));
	}

	@Override
	protected void fireResourceDeletionEvent(CertificateResource resource) {
		eventService.publishEvent(new CertificateResourceDeletedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceDeletionEvent(CertificateResource resource,
			Throwable t) {
		eventService.publishEvent(new CertificateResourceDeletedEvent(this,
				resource, t, getCurrentSession()));
	}

	@Override
	public CertificateResource updateResource(CertificateResource resource,
			String name, Map<String, String> properties)
			throws ResourceChangeException, AccessDeniedException {

		resource.setName(name);

		try {
			KeyPair pair = X509CertificateUtils.loadKeyPairFromPEM(
					new ByteArrayInputStream(resource.getPrivateKey().getBytes(
							"UTF-8")), null);
			Certificate cert = populateCertificate(properties, pair);

			ByteArrayOutputStream certFile = new ByteArrayOutputStream();
			X509CertificateUtils.saveCertificate(new Certificate[] { cert },
					certFile);

			resource.setCertificate(new String(certFile.toByteArray(), "UTF-8"));
			resource.setBundle(null);

			updateResource(resource, properties);

			return resource;
		} catch (CertificateException e) {
			log.error("Failed to generate certificate", e);
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"error.certificateError", e.getMessage());
		} catch (UnsupportedEncodingException e) {
			log.error("Failed to encode certificate", e);
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"error.certificateError", e.getMessage());
		} catch (InvalidPassphraseException e) {
			log.error("Failed to decrypt private key", e);
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"error.certificateError", e.getMessage());
		} catch (FileFormatException e) {
			log.error("Failed to decode certificate", e);
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"error.certificateError", e.getMessage());
		}
	}

	@Override
	public CertificateResource createResource(String name, Realm realm,
			Map<String, String> properties) throws ResourceCreationException,
			AccessDeniedException {

		CertificateResource resource = new CertificateResource();
		resource.setName(name);
		resource.setRealm(realm);

		CertificateType type = CertificateType.valueOf(properties
				.get("certType"));

		KeyPair pair = null;

		try {
			switch (type) {
			case RSA_1024:
				pair = X509CertificateUtils.generatePrivateKey("RSA", 1024);
				break;
			case RSA_2048:
				pair = X509CertificateUtils.generatePrivateKey("RSA", 2048);
				break;
			case DSA_1024:
				pair = X509CertificateUtils.generatePrivateKey("DSA", 1024);
				break;
			default:
				throw new ResourceCreationException(RESOURCE_BUNDLE,
						"error.unsupportedType");
			}

			Certificate cert = populateCertificate(properties, pair);

			ByteArrayOutputStream privateKeyFile = new ByteArrayOutputStream();
			X509CertificateUtils.saveKeyPair(pair, privateKeyFile);

			ByteArrayOutputStream certFile = new ByteArrayOutputStream();
			X509CertificateUtils.saveCertificate(new Certificate[] { cert },
					certFile);

			resource.setPrivateKey(new String(privateKeyFile.toByteArray(),
					"UTF-8"));
			resource.setCertificate(new String(certFile.toByteArray(), "UTF-8"));

			createResource(resource, properties);

			return resource;
		} catch (CertificateException e) {
			log.error("Failed to generate certificate", e);
			throw new ResourceCreationException(RESOURCE_BUNDLE,
					"error.certificateError", e.getMessage());
		} catch (UnsupportedEncodingException e) {
			log.error("Failed to encode certificate", e);
			throw new ResourceCreationException(RESOURCE_BUNDLE,
					"error.certificateError", e.getMessage());
		}
	}

	private Certificate populateCertificate(Map<String, String> properties,
			KeyPair pair) {
		return X509CertificateUtils.generateSelfSignedCertificate(
				properties.get("commonName"),
				properties.get("organizationalUnit"),
				properties.get("organization"), properties.get("location"),
				properties.get("state"), properties.get("country"), pair);
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate()
			throws AccessDeniedException {

		assertPermission(CertificateResourcePermission.READ);

		return repository.getPropertyCategories(null);
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate(
			CertificateResource resource) throws AccessDeniedException {

		assertPermission(CertificateResourcePermission.READ);

		return repository.getPropertyCategories(resource);
	}

	@Override
	public KeyStore getDefaultCertificate() throws ResourceCreationException,
			AccessDeniedException {

		CertificateResource resource;
		try {
			resource = getResourceByName("default");
		} catch (ResourceNotFoundException e) {
			Map<String, String> properties = new HashMap<String, String>();
			properties.put("certType", "RSA_2048");
			properties.put("commonName", "localhost");
			properties.put("organizationalUnit", "Default");
			properties.put("organization", "Default");
			properties.put("location", "Unknown");
			properties.put("state", "Unknown");
			properties.put("country", "US");

			resource = createResource("default",
					realmService.getDefaultRealm(), properties);
		}

		try {
			ByteArrayInputStream keyStream = new ByteArrayInputStream(resource
					.getPrivateKey().getBytes("UTF-8"));
			ByteArrayInputStream certStream = new ByteArrayInputStream(resource
					.getCertificate().getBytes("UTF-8"));
			ByteArrayInputStream caStream = null;

			if (!StringUtils.isEmpty(resource.getBundle())) {
				caStream = new ByteArrayInputStream(resource.getBundle()
						.getBytes("UTF-8"));
			}

			return loadPEMCertificate(keyStream, certStream, caStream, null,
					"changeit".toCharArray());

		} catch (UnsupportedEncodingException e) {
			log.error("Failed to encode certificate", e);
			throw new ResourceCreationException(RESOURCE_BUNDLE,
					"error.certificateError", e.getMessage());
		} catch (CertificateException e) {
			log.error("Failed to generate certificate", e);
			throw new ResourceCreationException(RESOURCE_BUNDLE,
					"error.certificateError", e.getMessage());
		} catch (MismatchedCertificateException e) {
			log.error("Failed to load certificate", e);
			throw new ResourceCreationException(RESOURCE_BUNDLE,
					"error.certificateError", e.getMessage());
		}
	}

	protected KeyStore loadPEMCertificate(InputStream keyStream,
			InputStream certStream, InputStream caStream, char[] keyPassphrase,
			char[] keystorePassphrase) throws CertificateException,
			MismatchedCertificateException {

		try {
			if (caStream != null) {
				return X509CertificateUtils.createKeystore(X509CertificateUtils
						.loadKeyPairFromPEM(keyStream, keyPassphrase),
						X509CertificateUtils.validateChain(X509CertificateUtils
								.loadCertificateChainFromPEM(caStream),
								X509CertificateUtils
										.loadCertificateFromPEM(certStream)),
						"hypersocket", keystorePassphrase);
			} else {
				return X509CertificateUtils.createKeystore(X509CertificateUtils
						.loadKeyPairFromPEM(keyStream, keyPassphrase),
						new X509Certificate[] { X509CertificateUtils
								.loadCertificateFromPEM(certStream) },
						"hypersocket", keystorePassphrase);
			}
		} catch (MismatchedCertificateException ex) {
			throw ex;
		} catch (Exception e) {
			throw new CertificateException(
					"Failed to load key/certificate files", e);
		}

	}

	private InputStream toInputStream(String str) {
		try {
			return new ByteArrayInputStream(str.getBytes("UTF-8"));
		} catch (UnsupportedEncodingException e) {
			return new ByteArrayInputStream(str.getBytes());
		}
	}
	
	private KeyPair loadKeyPair(CertificateResource resource)
			throws CertificateException, UnsupportedEncodingException,
			InvalidPassphraseException, FileFormatException {
		return X509CertificateUtils.loadKeyPairFromPEM(
				new ByteArrayInputStream(resource.getPrivateKey().getBytes(
						"UTF-8")), null);
	}

	@Override
	public String generateCSR(CertificateResource resource) throws UnsupportedEncodingException, Exception {

		KeyPair pair = loadKeyPair(resource);

		return new String(X509CertificateUtils.generatePKCS10(pair.getPrivate(),
				pair.getPublic(), resource.getCommonName(),
				resource.getOrganizationalUnit(), resource.getOrganization(),
				resource.getLocation(), resource.getState(),
				resource.getCountry()), "UTF-8");
	}

	@Override
	public void updateCertificate(CertificateResource resource,
			MultipartFile file, MultipartFile bundle) throws ResourceChangeException {
		
		
		try {
			X509Certificate cert = X509CertificateUtils.loadCertificateFromPEM(file
					.getInputStream());

			X509Certificate[] ca = X509CertificateUtils
					.loadCertificateChainFromPEM(bundle.getInputStream());

			X509CertificateUtils.validateChain(ca, cert);

			KeyPair pair = loadKeyPair(resource);

			if (!pair.getPublic().equals(cert.getPublicKey())) {
				throw new MismatchedCertificateException("The certificate does not match the private key.");
			}

			ByteArrayOutputStream certStream = new ByteArrayOutputStream();
			X509CertificateUtils.saveCertificate(new Certificate[] { cert }, certStream);
			
			ByteArrayOutputStream caStream = new ByteArrayOutputStream();
			X509CertificateUtils.saveCertificate(ca, caStream);
			
			resource.setCertificate(new String(certStream.toByteArray(), "UTF-8"));
			resource.setBundle(new String(caStream.toByteArray(), "UTF-8"));

			updateResource(resource, new HashMap<String,String>());
			
		} catch (CertificateException | ResourceChangeException | IOException
				| FileFormatException | InvalidPassphraseException
				| MismatchedCertificateException | AccessDeniedException e) {
			log.error("Failed to generate certificate", e);
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"error.certificateError", e.getMessage());
		}
		
	}

	@Override
	public void importPrivateKey(MultipartFile key, String passphrase,
			MultipartFile file, MultipartFile bundle) throws ResourceCreationException, InvalidPassphraseException {
		
		try {
			X509Certificate cert = X509CertificateUtils.loadCertificateFromPEM(file
					.getInputStream());

			X509Certificate[] ca = X509CertificateUtils
					.loadCertificateChainFromPEM(bundle.getInputStream());

			X509CertificateUtils.validateChain(ca, cert);

			KeyPair pair = X509CertificateUtils.loadKeyPairFromPEM(key.getInputStream(), passphrase.toCharArray());
			
			if (!pair.getPublic().equals(cert.getPublicKey())) {
				throw new MismatchedCertificateException("The certificate does not match the private key.");
			}
			
			ByteArrayOutputStream privateKeyFile = new ByteArrayOutputStream();
			X509CertificateUtils.saveKeyPair(pair, privateKeyFile);
			
			ByteArrayOutputStream certStream = new ByteArrayOutputStream();
			X509CertificateUtils.saveCertificate(new Certificate[] { cert }, certStream);
			
			ByteArrayOutputStream caStream = new ByteArrayOutputStream();
			X509CertificateUtils.saveCertificate(ca, caStream);
		
			CertificateResource resource = new CertificateResource();
			
			resource.setName(cert.getSubjectDN().getName());
			resource.setPrivateKey(new String(privateKeyFile.toByteArray(),	"UTF-8"));
			resource.setCertificate(new String(certStream.toByteArray(), "UTF-8"));
			resource.setBundle(new String(caStream.toByteArray(), "UTF-8"));

			createResource(resource, new HashMap<String,String>());
			
		} catch (CertificateException | IOException
				| FileFormatException
				| MismatchedCertificateException | AccessDeniedException e) {
			log.error("Failed to generate certificate", e);
			throw new ResourceCreationException(RESOURCE_BUNDLE,
					"error.certificateError", e.getMessage());
		}
		
	}

}
