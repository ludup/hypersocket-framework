package com.hypersocket.certificates;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.hypersocket.certs.InvalidPassphraseException;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceException;

public interface CertificateResourceService extends
		AbstractResourceService<CertificateResource> {
	
	static final String RESOURCE_BUNDLE = "CertificateResourceService";
	
	void registerProvider(CertificateProvider provider);
	
	CertificateResource updateResource(CertificateResource resourceById,
			String name, Map<String, String> properties)
			throws ResourceException, AccessDeniedException;

	CertificateResource createResource(String name, Realm realm,
			Map<String, String> properties, boolean system)
			throws ResourceException, AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate()
			throws AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate(
			CertificateResource resource) throws AccessDeniedException;

	KeyStore getDefaultCertificate() throws ResourceException,
			AccessDeniedException;

	String generateCSR(CertificateResource resourceById)
			throws UnsupportedEncodingException, Exception;

	void updateCertificate(CertificateResource resource, MultipartFile file,
			MultipartFile bundle) throws ResourceException;

	CertificateResource importPrivateKey(MultipartFile key, String passphrase,
			MultipartFile file, MultipartFile bundle)
			throws ResourceException, InvalidPassphraseException;
	
	CertificateResource importPrivateKey(InputStream key, String passphrase,
			InputStream file, InputStream bundle)
			throws ResourceException, InvalidPassphraseException;

	CertificateResource importPfx(MultipartFile key, String passphrase) throws ResourceException, AccessDeniedException;

	CertificateResource createResource(String name, Realm realm,
			CertificateType type, String cn, String ou, String o, String l,
			String s, String c, boolean system)
			throws ResourceException, AccessDeniedException;

	KeyStore getResourceKeystore(CertificateResource resourceByName,
			String string, String string2) throws ResourceException;

	CertificateResource replacePfx(CertificateResource resource, MultipartFile key,
			String passphrase) throws AccessDeniedException, ResourceException, IOException;

	CertificateResource replacePrivateKey(CertificateResource resourceById, MultipartFile key,
			String passphrase, MultipartFile file, MultipartFile bundle) throws InvalidPassphraseException, ResourceException, IOException;

	KeyStore getResourceKeystore(CertificateResource resource) throws ResourceException;

	void updateCertificate(CertificateResource resource, InputStream file, InputStream bundle)
			throws ResourceException;

	CertificateResource replacePfx(CertificateResource resource, InputStream pfx, String passphrase)
			throws AccessDeniedException, ResourceException;

	CertificateResource replacePrivateKey(CertificateResource resource, InputStream key, String passphrase,
			InputStream file, InputStream bundle) throws ResourceException, InvalidPassphraseException;

	Map<String, CertificateProvider> getProviders();

	KeyStore getKeystoreWithCertificates(CertificateResource defaultCertificate, 
			Collection<CertificateResource> certificates)
			throws ResourceException, AccessDeniedException;

	X509Certificate getX509Certificate(CertificateResource resource) throws CertificateException;

	void sendExpiringNotification(CertificateResource resource, X509Certificate x509);

	void updateCertificate(CertificateResource resource) throws ResourceException, AccessDeniedException;
	
	public CertificateProvider getProvider(String providerId);

}
