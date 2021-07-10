package com.hypersocket.certificates;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.asn1.x500.AttributeTypeAndValue;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hypersocket.ApplicationContextServiceImpl;
import com.hypersocket.certificates.events.CertificateResourceCreatedEvent;
import com.hypersocket.certificates.events.CertificateResourceDeletedEvent;
import com.hypersocket.certificates.events.CertificateResourceEvent;
import com.hypersocket.certificates.events.CertificateResourceUpdatedEvent;
import com.hypersocket.certificates.jobs.CertificateExpiringMessageRepository;
import com.hypersocket.certs.FileFormatException;
import com.hypersocket.certs.InvalidPassphraseException;
import com.hypersocket.certs.MismatchedCertificateException;
import com.hypersocket.certs.X509CertificateUtils;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.json.utils.HypersocketUtils;
import com.hypersocket.message.MessageResourceService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.PropertyChange;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.resource.TransactionAdapter;
import com.hypersocket.upgrade.UpgradeService;

@Service
public class CertificateResourceServiceImpl extends
		AbstractResourceServiceImpl<CertificateResource> implements
		CertificateResourceService {

	static Logger log = LoggerFactory
			.getLogger(CertificateResourceServiceImpl.class);

	public static final String DEFAULT_CERTIFICATE_NAME = "Default SSL Certificate";

	public static final String MESSAGE_CERTIFICATE_CREATED = "message.certificateCreated";
	public static final String MESSAGE_CERTIFICATE_UPDATED = "message.certificateUpdated";
	public static final String MESSAGE_CERTIFICATE_EXPIRING = "message.certificateExpiring";
	public static final String MESSAGE_CERTIFICATE_EXPIRED=  "message.certificateExpired";
	
	@Autowired
	private CertificateResourceRepository repository;

	@Autowired
	private I18NService i18nService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private RealmService realmService;

	@Autowired
	private EventService eventService;
	
	@Autowired
	private MessageResourceService messageService; 
	
	@Autowired
	private CertificateExpiringMessageRepository certificateExpiryMessageRepository;
	
	private Map<String, CertificateProvider> providers = new HashMap<>();
	
	public CertificateResourceServiceImpl() {
		super("certificates");
	}
	
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
		eventService.registerEvent(CertificateResourceEvent.class,
				RESOURCE_BUNDLE, this);
		eventService.registerEvent(CertificateResourceCreatedEvent.class,
				RESOURCE_BUNDLE, this);
		eventService.registerEvent(CertificateResourceUpdatedEvent.class,
				RESOURCE_BUNDLE, this);
		eventService.registerEvent(CertificateResourceDeletedEvent.class,
				RESOURCE_BUNDLE, this);

		EntityResourcePropertyStore.registerResourceService(CertificateResource.class, repository);
		
		registerProvider(new DefaultCertificateProvider());
		
		messageService.registerI18nMessage(RESOURCE_BUNDLE, MESSAGE_CERTIFICATE_CREATED, CertificateResolver.getVariables());
		messageService.registerI18nMessage(RESOURCE_BUNDLE, MESSAGE_CERTIFICATE_UPDATED, CertificateResolver.getVariables());
		messageService.registerI18nMessage(RESOURCE_BUNDLE, MESSAGE_CERTIFICATE_EXPIRED, CertificateResolver.getVariables());
		messageService.registerI18nMessage(RESOURCE_BUNDLE, MESSAGE_CERTIFICATE_EXPIRING, CertificateResolver.getVariables(), false, certificateExpiryMessageRepository);
	
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
	
	protected Class<CertificateResource> getResourceClass() {
		return CertificateResource.class;
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
	protected boolean fireNonStandardEvents(CertificateResource issue, List<PropertyChange> changes) {
		return true;
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
	public void updateCertificate(CertificateResource resource) throws ResourceException, AccessDeniedException {
		updateResource(resource, new TransactionAdapter<CertificateResource>() {

			@Override
			public void afterOperation(CertificateResource resource, Map<String, String> properties)
					throws ResourceException {
				sendCertificateNotification(resource, MESSAGE_CERTIFICATE_UPDATED);
			}
			
		});
	}
	
	@Override
	public CertificateResource updateResource(CertificateResource resource,
			String name, Map<String, String> properties)
			throws ResourceException, AccessDeniedException {

		resource.setName(name);

		updateResource(resource, properties, new TransactionAdapter<CertificateResource>() {

			
			@Override
			public void beforeOperation(CertificateResource resource, Map<String, String> properties)
					throws ResourceException {
				try {
					getProvider(resource.getProvider()).update(resource, name, properties);
				} catch (CertificateException | UnsupportedEncodingException | InvalidPassphraseException
						| FileFormatException | AccessDeniedException e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
			}

			@Override
			public void afterOperation(CertificateResource resource, Map<String, String> properties)
					throws ResourceException {
				sendCertificateNotification(resource, MESSAGE_CERTIFICATE_UPDATED);
			}
			
		});
		
		return resource;
	}

	@Override
	public CertificateResource createResource(String name, Realm realm,
			Map<String, String> properties, boolean system)
			throws ResourceException, AccessDeniedException {

		CertificateResource resource = new CertificateResource();
		resource.setName(name);
		resource.setRealm(realm);
		resource.setSystem(system);
		
		createResource(resource, properties, new TransactionAdapter<CertificateResource>() {

			@Override
			public void beforeOperation(CertificateResource resource, Map<String, String> properties)
					throws ResourceException {
				try {
					getProvider(resource.getProvider()).create(resource, properties);
				} catch (CertificateException | UnsupportedEncodingException | AccessDeniedException e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
			}

			@Override
			public void afterOperation(CertificateResource resource, Map<String, String> properties)
					throws ResourceException {
				sendCertificateNotification(resource, MESSAGE_CERTIFICATE_CREATED);
			}
			
		});
		return resource;
	}

	private CertificateProvider getProvider(String providerId) {
		CertificateProvider provider = providers.get(StringUtils.isBlank(providerId) ? "default" : providerId);
		if(provider == null)
			throw new IllegalArgumentException(String.format("No provider with ID of %s", providerId));
		return provider;
	}

	@Override
	public CertificateResource createResource(String name, Realm realm,
			CertificateType type, String cn, String ou, String o, String l,
			String s, String c, boolean system)
			throws ResourceException, AccessDeniedException {

		Map<String, String> properties = new HashMap<String, String>();

		properties.put("certType", type.toString());
		properties.put("commonName", cn);
		properties.put("organizationalUnit", ou);
		properties.put("organization", o);
		properties.put("location", l);
		properties.put("state", s);
		properties.put("country", c);

		return createResource(name, realm, properties, system);
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
	public KeyStore getDefaultCertificate() throws ResourceException, AccessDeniedException {

		CertificateResource resource;
		try {
			resource = getResourceByName(DEFAULT_CERTIFICATE_NAME);
		} catch (ResourceNotFoundException e) {
			Map<String, String> properties = new HashMap<String, String>();
			properties.put("certType", "RSA_2048");
			properties.put("commonName", "localhost");
			properties.put("organizationalUnit", "Default");
			properties.put("organization", "Default");
			properties.put("location", "Unknown");
			properties.put("state", "Unknown");
			properties.put("country", "US");

			resource = createResource(DEFAULT_CERTIFICATE_NAME,
					realmService.getSystemRealm(), properties, true);

		}

		return getResourceKeystore(resource, "hypersocket", "changeit");
	}

	protected KeyStore loadPEMCertificate(InputStream keyStream,
			InputStream certStream, InputStream caStream, String alias,
			char[] keyPassphrase, char[] keystorePassphrase)
			throws CertificateException, MismatchedCertificateException {

		try {

			if (caStream != null) {
				X509Certificate[] ca = ArrayUtils.add(X509CertificateUtils
						.loadCertificateChainFromPEM(caStream), X509CertificateUtils
						.loadCertificateFromPEM(certStream));
				ArrayUtils.reverse(ca);
				
				return X509CertificateUtils.createKeystore(X509CertificateUtils
						.loadKeyPairFromPEM(keyStream, keyPassphrase),
						 ca,
						alias, keystorePassphrase);
			} else {
				return X509CertificateUtils.createKeystore(X509CertificateUtils
						.loadKeyPairFromPEM(keyStream, keyPassphrase),
						new X509Certificate[] { X509CertificateUtils
								.loadCertificateFromPEM(certStream) }, alias,
						keystorePassphrase);
			}
		} catch (MismatchedCertificateException ex) {
			throw ex;
		} catch (Exception e) {
			throw new CertificateException(
					"Failed to load key/certificate files", e);
		}

	}
	
	protected void loadPEMCertificate(CertificateResource resource,
			String alias, String password, KeyStore keystore) throws ResourceException {
		
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

			loadPEMCertificate(keyStream, certStream, caStream, alias,
					null, password.toCharArray(), keystore);

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
	
	
	protected void loadPEMCertificate(InputStream keyStream,
			InputStream certStream, InputStream caStream, String alias,
			char[] keyPassphrase, char[] keystorePassphrase, KeyStore keystore)
			throws CertificateException, MismatchedCertificateException {

		mergeKeystores(keystore, 
				loadPEMCertificate(keyStream, certStream, caStream, alias, keyPassphrase, keystorePassphrase),
				"changeit");
	}

	private void mergeKeystores(KeyStore newKeystore, KeyStore oldKeystore, String sPassword) throws CertificateException {
		
		try {

			Enumeration<?> enumeration = oldKeystore.aliases();
			while (enumeration.hasMoreElements()) {
				String alias = (String) enumeration.nextElement();
				Key key = oldKeystore.getKey(alias, sPassword.toCharArray());
				Certificate[] certs = oldKeystore.getCertificateChain(alias);
				newKeystore.setKeyEntry(alias, key, sPassword.toCharArray(), certs);
			}
		} catch (Exception e) {
			throw new CertificateException("Failed to load key/certificate files", e);
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
	public String generateCSR(CertificateResource resource)
			throws UnsupportedEncodingException, Exception {

		KeyPair pair = loadKeyPair(resource);

		return new String(X509CertificateUtils.generatePKCS10(
				pair.getPrivate(), pair.getPublic(), resource.getCommonName(),
				resource.getOrganizationalUnit(), resource.getOrganization(),
				resource.getLocation(), resource.getState(),
				resource.getCountry()), "UTF-8");
	}

	@Override
	public void updateCertificate(CertificateResource resource,
			MultipartFile file, MultipartFile bundle)
			throws ResourceException {
		try {
			updateCertificate(resource, file.getInputStream(), bundle==null ? null : bundle.getInputStream());
		} catch (IOException e) {
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"error.certificateError", e.getMessage());
		}
	}
	
	@Override
	public void updateCertificate(CertificateResource resource,
			InputStream file, InputStream bundle)
			throws ResourceException {

		try {
			X509Certificate cert = X509CertificateUtils
					.loadCertificateFromPEM(file);

			X509Certificate[] ca = null;
			
			if(bundle!=null) {
				ca = X509CertificateUtils.loadCertificateChainFromPEM(bundle);
				X509CertificateUtils.validateChain(ca, cert);
			}
			
			KeyPair pair = loadKeyPair(resource);

			if (!pair.getPublic().equals(cert.getPublicKey())) {
				throw new MismatchedCertificateException(
						"The certificate does not match the private key.");
			}

			ByteArrayOutputStream certStream = new ByteArrayOutputStream();
			X509CertificateUtils.saveCertificate(new Certificate[] { cert },
					certStream);

			if(ca!=null) {
				ByteArrayOutputStream caStream = new ByteArrayOutputStream();
				X509CertificateUtils.saveCertificate(ca, caStream);
				resource.setBundle(new String(caStream.toByteArray(), "UTF-8"));
			}
			resource.setCertificate(new String(certStream.toByteArray(), "UTF-8"));

			updateResource(resource);

		} catch (CertificateException | ResourceChangeException | IOException
				| FileFormatException | InvalidPassphraseException
				| MismatchedCertificateException | AccessDeniedException e) {
			log.error("Failed to generate certificate", e);
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"error.certificateError", e.getMessage());
		} finally {
			IOUtils.closeQuietly(file);
			IOUtils.closeQuietly(bundle);
		}

	}

	@Override
	public CertificateResource importPrivateKey(InputStream key,
			String passphrase, InputStream file, InputStream bundle)
			throws ResourceException, InvalidPassphraseException {

		CertificateResource resource = new CertificateResource();

		try {

			doInternalPrivateKey(resource, key, passphrase, file, bundle);
			resource.setRealm(getCurrentRealm());
			createResource(resource, new HashMap<String, String>());
			return resource;
		} catch (CertificateException | IOException | FileFormatException
				| MismatchedCertificateException | AccessDeniedException e) {
			log.error("Failed to generate certificate", e);
			throw new ResourceCreationException(RESOURCE_BUNDLE,
					"error.certificateError", e.getMessage());
		}

	}
	
	@Override
	public CertificateResource importPrivateKey(MultipartFile key,
			String passphrase, MultipartFile file, MultipartFile bundle)
			throws ResourceException, InvalidPassphraseException {

		CertificateResource resource = new CertificateResource();

		try {

			doInternalPrivateKey(resource, key, passphrase, file, bundle);
			resource.setRealm(getCurrentRealm());
			createResource(resource, new HashMap<String, String>());
			return resource;
		} catch (CertificateException | IOException | FileFormatException
				| MismatchedCertificateException | AccessDeniedException e) {
			log.error("Failed to generate certificate", e);
			throw new ResourceCreationException(RESOURCE_BUNDLE,
					"error.certificateError", e.getMessage());
		}

	}

	@Override
	public CertificateResource replacePrivateKey(CertificateResource resource,
			MultipartFile key, String passphrase, MultipartFile file,
			MultipartFile bundle) throws ResourceException,
			InvalidPassphraseException, IOException {
		return replacePrivateKey(resource, key.getInputStream(), passphrase, 
				file.getInputStream(), bundle.getInputStream());
	}
	
	@Override
	public CertificateResource replacePrivateKey(CertificateResource resource,
			InputStream key, String passphrase, InputStream file,
			InputStream bundle) throws ResourceException,
			InvalidPassphraseException {

		try {

			doInternalPrivateKey(resource, key, passphrase, file, bundle);
			updateResource(resource, new HashMap<String, String>());
			return resource;
		} catch (CertificateException | IOException | FileFormatException
				| MismatchedCertificateException | AccessDeniedException e) {
			log.error("Failed to replace certificate", e);
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"error.certificateError", e.getMessage());
		}
	}

	private void doInternalPrivateKey(CertificateResource resource,
			MultipartFile key, String passphrase, MultipartFile file,
			MultipartFile bundle) throws InvalidPassphraseException,
			CertificateException, IOException, FileFormatException,
			MismatchedCertificateException {
		doInternalPrivateKey(resource, key.getInputStream(), 
				passphrase, 
				file.getInputStream(), 
				bundle==null ? null : bundle.getInputStream());
		
	}
	
	
	private void doInternalPrivateKey(CertificateResource resource,
			InputStream key, String passphrase, InputStream file,
			InputStream bundle) throws InvalidPassphraseException,
			CertificateException, IOException, FileFormatException,
			MismatchedCertificateException {
		
		X509Certificate cert = X509CertificateUtils.loadCertificateFromPEM(file);

		X509Certificate[] ca = null;
		
		if(bundle!=null) {
			ca = X509CertificateUtils
					.loadCertificateChainFromPEM(bundle);
			X509CertificateUtils.validateChain(ca, cert);
		}
		
		KeyPair pair = X509CertificateUtils.loadKeyPairFromPEM(key, 
				passphrase.toCharArray());

		if (!pair.getPublic().equals(cert.getPublicKey())) {
			throw new MismatchedCertificateException(
					"The certificate does not match the private key.");
		}

		ByteArrayOutputStream privateKeyFile = new ByteArrayOutputStream();
		X509CertificateUtils.saveKeyPair(pair, privateKeyFile);

		ByteArrayOutputStream certStream = new ByteArrayOutputStream();
		X509CertificateUtils.saveCertificate(new Certificate[] { cert },
				certStream);

		if(ca!=null) {
			ByteArrayOutputStream caStream = new ByteArrayOutputStream();
			X509CertificateUtils.saveCertificate(ca, caStream);
			resource.setBundle(new String(caStream.toByteArray(), "UTF-8"));
		}
		
		X500Name x500name = new JcaX509CertificateHolder(cert).getSubject();
		RDN cn = x500name.getRDNs(BCStyle.CN)[0];
		for (RDN rdn : x500name.getRDNs()) {
			for (AttributeTypeAndValue v : rdn.getTypesAndValues()) {
				log.info(v.getType().toString() + ": "
						+ IETFUtils.valueToString(v.getValue()));
			}
		}
		if (!DEFAULT_CERTIFICATE_NAME.equals(resource.getName())) {
			resource.setName(IETFUtils.valueToString(cn.getFirst().getValue()));
		}
		resource.setCommonName(IETFUtils
				.valueToString(cn.getFirst().getValue()));
		resource.setCountry("");
		resource.setLocation("");
		resource.setOrganization("");
		resource.setOrganizationalUnit("");
		resource.setState("");
		resource.setPrivateKey(new String(privateKeyFile.toByteArray(), "UTF-8"));
		resource.setCertificate(new String(certStream.toByteArray(), "UTF-8"));
		
		if (cert.getNotBefore() != null) {
			resource.setIssueDate(cert.getNotBefore());
		}
		if (cert.getNotAfter() != null) {
			resource.setExpiryDate(cert.getNotAfter());
		}

		

	}

	@Override
	public CertificateResource importPfx(MultipartFile pfx, String passphrase)
			throws ResourceException, AccessDeniedException {

		CertificateResource resource = new CertificateResource();

		try {
			internalDoPfx(resource, pfx, passphrase);
			resource.setRealm(getCurrentRealm());
			createResource(resource, new HashMap<String, String>());
			return resource;
		} catch (IOException | CertificateException | UnrecoverableKeyException
				| KeyStoreException | NoSuchAlgorithmException
				| NoSuchProviderException | MismatchedCertificateException e) {
			throw new ResourceCreationException(
					CertificateResourceServiceImpl.RESOURCE_BUNDLE,
					"error.genericError", e.getMessage());

		}

	}

	@Override
	public CertificateResource replacePfx(CertificateResource resource,
			MultipartFile pfx, String passphrase) throws AccessDeniedException, ResourceException, IOException {
		return replacePfx(resource, pfx.getInputStream(), passphrase);
	}
	
	@Override
	public CertificateResource replacePfx(CertificateResource resource,
			InputStream pfx, String passphrase) throws AccessDeniedException, ResourceException {

		try {
			internalDoPfx(resource, pfx, passphrase);
			updateResource(resource, new HashMap<String, String>());
			return resource;
		} catch (KeyStoreException ke) {
			throw new ResourceChangeException(
					CertificateResourceServiceImpl.RESOURCE_BUNDLE,
					"error.keyError", ke.getMessage());
		} catch (IOException | CertificateException | UnrecoverableKeyException
				| NoSuchAlgorithmException | NoSuchProviderException
				| MismatchedCertificateException e) {
			throw new ResourceChangeException(
					CertificateResourceServiceImpl.RESOURCE_BUNDLE,
					"error.genericError", e.getMessage());

		}
	}

	private void internalDoPfx(CertificateResource resource, MultipartFile pfx,
			String passphrase) throws AccessDeniedException,
			UnrecoverableKeyException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException,
			NoSuchProviderException, IOException,
			MismatchedCertificateException {
		internalDoPfx(resource, pfx.getInputStream(), passphrase);
	}
	
	private void internalDoPfx(CertificateResource resource, InputStream pfx,
			String passphrase) throws AccessDeniedException,
			UnrecoverableKeyException, KeyStoreException,
			NoSuchAlgorithmException, CertificateException,
			NoSuchProviderException, IOException,
			MismatchedCertificateException {
		
		KeyStore keystore = null;

		try {
			keystore = X509CertificateUtils.loadKeyStoreFromPFX(
					pfx, passphrase.toCharArray());
		} catch (IOException ie) {
			throw new KeyStoreException(ie.getMessage());
		}
		Enumeration<String> aliases = keystore.aliases();
		while (aliases.hasMoreElements()) {
			String alias = aliases.nextElement();
			if (keystore.isKeyEntry(alias)) {

				Key key = keystore.getKey(alias, passphrase.toCharArray());
				if (key instanceof PrivateKey) {
					X509Certificate cert = (X509Certificate) keystore
							.getCertificate(alias);

					Certificate[] chain = keystore.getCertificateChain(alias);

					PublicKey publicKey = cert.getPublicKey();
					KeyPair pair = new KeyPair(publicKey, (PrivateKey) key);

					ByteArrayOutputStream privateKeyFile = new ByteArrayOutputStream();
					X509CertificateUtils.saveKeyPair(pair, privateKeyFile);
					resource.setPrivateKey(new String(privateKeyFile
							.toByteArray(), "UTF-8"));

					List<Certificate> bundle = new ArrayList<Certificate>(
							Arrays.asList(chain));
					if (bundle.size() > 1) {
						bundle.remove(0);
					}

					Certificate[] rootAndInters = bundle
							.toArray(new Certificate[0]);
					X509CertificateUtils.validateChain(rootAndInters, cert);

					if (!pair.getPublic().equals(cert.getPublicKey())) {
						throw new MismatchedCertificateException();
					}

					ByteArrayOutputStream caStream = new ByteArrayOutputStream();
					X509CertificateUtils.saveCertificate(rootAndInters,
							caStream);
					resource.setBundle(new String(caStream.toByteArray(),
							"UTF-8"));

					ByteArrayOutputStream certStream = new ByteArrayOutputStream();
					X509CertificateUtils.saveCertificate(
							new Certificate[] { cert }, certStream);
					resource.setCertificate(new String(
							certStream.toByteArray(), "UTF-8"));

					X500Name x500name = new JcaX509CertificateHolder(cert)
							.getSubject();
					RDN cn = x500name.getRDNs(BCStyle.CN)[0];
					for (RDN rdn : x500name.getRDNs()) {
						for (AttributeTypeAndValue v : rdn.getTypesAndValues()) {
							log.info(v.getType().toString() + ": "
									+ IETFUtils.valueToString(v.getValue()));
						}
					}
					if (resource.getName()==null || !resource.getName().equals(DEFAULT_CERTIFICATE_NAME)) {
						resource.setName(IETFUtils.valueToString(cn.getFirst()
								.getValue()));
					}
					resource.setCommonName(IETFUtils.valueToString(cn
							.getFirst().getValue()));
					resource.setCountry("");
					resource.setLocation("");
					resource.setOrganization("");
					resource.setOrganizationalUnit("");
					resource.setState("");
					
					if (cert.getNotBefore() != null) {
						resource.setIssueDate(cert.getNotBefore());
					}
					if (cert.getNotAfter() != null) {
						resource.setExpiryDate(cert.getNotAfter());
					}

				}
			}
		}

	}

	@Override
	public void registerProvider(CertificateProvider provider) {
		if(providers.containsKey(provider.getResourceKey()))
			throw new IllegalArgumentException(String.format("The provider %s is alread registered.", provider.getResourceKey()));
		providers.put(provider.getResourceKey(), provider);
	}

	@Override
	public KeyStore getResourceKeystore(CertificateResource resource) throws ResourceException {
		return getResourceKeystore(resource, "hypersocket", "changeit");
	}
	
	@Override
	public KeyStore getKeystoreWithCertificates(CertificateResource defaultCertificate, 
			Collection<CertificateResource> certificates) throws  ResourceException, AccessDeniedException {
		
		KeyStore ks = getResourceKeystore(defaultCertificate, "hypersocket", "changeit");
		for(CertificateResource resource : certificates) {
			loadPEMCertificate(resource, resource.getCommonName(), "changeit", ks);
		}
		return ks;
	}
	
	@Override
	public KeyStore getResourceKeystore(CertificateResource resource,
			String alias, String password) throws ResourceException {

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

			return loadPEMCertificate(keyStream, certStream, caStream, alias,
					null, password.toCharArray());

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

	@Override
	public Map<String, CertificateProvider> getProviders() {
		return Collections.unmodifiableMap(providers);
	}


	private void sendCertificateNotification(CertificateResource resource, String message) {
		
		if(ApplicationContextServiceImpl.getInstance().getBean("upgradeService", UpgradeService.class).isDone()) {
			try {
		        messageService.sendMessage(message, resource.getRealm(), new CertificateResolver(resource, getX509Certificate(resource)), permissionService.iteratePrincipalsByRole(resource.getRealm(), 
						permissionService.getSystemAdministratorRole(),
						permissionService.getRealmAdministratorRole(resource.getRealm())));
			} catch (AccessDeniedException | CertificateException | ResourceException e) {
				log.error("Failed to send certificate message", e);
			}
		}
	}

	@Override
	public X509Certificate getX509Certificate(CertificateResource resource) throws CertificateException {
		
		CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
        return (X509Certificate) certificateFactory.generateCertificate(new ByteArrayInputStream(
        		HypersocketUtils.getUTF8Bytes(resource.getCertificate())));
	}
	
	@Override
	public void sendExpiringNotification(CertificateResource resource, X509Certificate x509) {
		
		if(x509.getNotAfter().before(new Date())) {
			sendCertificateNotification(resource, MESSAGE_CERTIFICATE_EXPIRED);
		} else {
			sendCertificateNotification(resource, MESSAGE_CERTIFICATE_EXPIRING);
		}
	}
}
