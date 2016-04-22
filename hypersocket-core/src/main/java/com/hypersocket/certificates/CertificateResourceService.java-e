package com.hypersocket.certificates;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyStore;
import java.util.Collection;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.hypersocket.certs.InvalidPassphraseException;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;

public interface CertificateResourceService extends
		AbstractResourceService<CertificateResource> {
	
	static final String RESOURCE_BUNDLE = "CertificateResourceService";
	
	CertificateResource updateResource(CertificateResource resourceById,
			String name, Map<String, String> properties)
			throws ResourceChangeException, AccessDeniedException;

	CertificateResource createResource(String name, Realm realm,
			Map<String, String> properties, boolean system)
			throws ResourceCreationException, AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate()
			throws AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate(
			CertificateResource resource) throws AccessDeniedException;

	KeyStore getDefaultCertificate() throws ResourceCreationException,
			AccessDeniedException;

	String generateCSR(CertificateResource resourceById)
			throws UnsupportedEncodingException, Exception;

	void updateCertificate(CertificateResource resource, MultipartFile file,
			MultipartFile bundle) throws ResourceChangeException;

	CertificateResource importPrivateKey(MultipartFile key, String passphrase,
			MultipartFile file, MultipartFile bundle)
			throws ResourceCreationException, InvalidPassphraseException;

	CertificateResource importPfx(MultipartFile key, String passphrase) throws ResourceCreationException, AccessDeniedException;

	CertificateResource createResource(String name, Realm realm,
			CertificateType type, String cn, String ou, String o, String l,
			String s, String c, boolean system)
			throws ResourceCreationException, AccessDeniedException;

	KeyStore getResourceKeystore(CertificateResource resourceByName,
			String string, String string2) throws ResourceCreationException;

	CertificateResource replacePfx(CertificateResource resource, MultipartFile key,
			String passphrase) throws AccessDeniedException, ResourceChangeException, IOException;

	CertificateResource replacePrivateKey(CertificateResource resourceById, MultipartFile key,
			String passphrase, MultipartFile file, MultipartFile bundle) throws InvalidPassphraseException, ResourceChangeException, IOException;

	KeyStore getResourceKeystore(CertificateResource resource) throws ResourceCreationException;

	void updateCertificate(CertificateResource resource, InputStream file, InputStream bundle)
			throws ResourceChangeException;

	CertificateResource replacePfx(CertificateResource resource, InputStream pfx, String passphrase)
			throws AccessDeniedException, ResourceChangeException;

	CertificateResource replacePrivateKey(CertificateResource resource, InputStream key, String passphrase,
			InputStream file, InputStream bundle) throws ResourceChangeException, InvalidPassphraseException;

}
