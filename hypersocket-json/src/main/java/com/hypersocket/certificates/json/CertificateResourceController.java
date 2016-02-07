package com.hypersocket.certificates.json;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.URLEncoder;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
import org.bouncycastle.openssl.jcajce.JcePEMEncryptorBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.multipart.MultipartFile;

import com.hypersocket.auth.json.AuthenticationRequired;
import com.hypersocket.auth.json.ResourceController;
import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.certificates.CertificateResource;
import com.hypersocket.certificates.CertificateResourceColumns;
import com.hypersocket.certificates.CertificateResourceService;
import com.hypersocket.certificates.CertificateResourceServiceImpl;
import com.hypersocket.certs.InvalidPassphraseException;
import com.hypersocket.certs.X509CertificateUtils;
import com.hypersocket.certs.json.CertificateStatus;
import com.hypersocket.i18n.I18N;
import com.hypersocket.json.RequestStatus;
import com.hypersocket.json.ResourceList;
import com.hypersocket.json.ResourceStatus;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.json.PropertyItem;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.resource.ResourceUpdate;
import com.hypersocket.session.json.SessionTimeoutException;
import com.hypersocket.tables.BootstrapTableResult;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.json.BootstrapTablePageProcessor;

@Controller
public class CertificateResourceController extends ResourceController {

	@Autowired
	CertificateResourceService resourceService;

	@AuthenticationRequired
	@RequestMapping(value = "certificates/list", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<CertificateResource> getResources(
			HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			return new ResourceList<CertificateResource>(
					resourceService.allResources());
		} finally {
			clearAuthenticatedContext();
		}
	}
	
	@AuthenticationRequired
	@RequestMapping(value = "certificates/table", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public BootstrapTableResult tableNetworkResources(
			final HttpServletRequest request, HttpServletResponse response)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return processDataTablesRequest(request,
					new BootstrapTablePageProcessor() {

						@Override
						public Column getColumn(String col) {
							return CertificateResourceColumns.valueOf(col.toUpperCase());
						}

						@Override
						public List<?> getPage(String searchColumn, String searchPattern, int start,
								int length, ColumnSort[] sorting)
								throws UnauthorizedException,
								AccessDeniedException {
							return resourceService.searchResources(
									sessionUtils.getCurrentRealm(request),
									searchColumn, searchPattern, start, length, sorting);
						}

						@Override
						public Long getTotalCount(String searchColumn, String searchPattern)
								throws UnauthorizedException,
								AccessDeniedException {
							return resourceService.getResourceCount(
									sessionUtils.getCurrentRealm(request),
									searchColumn, searchPattern);
						}
					});
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "certificates/template", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getResourceTemplate(
			HttpServletRequest request) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			return new ResourceList<PropertyCategory>(
					resourceService.getPropertyTemplate());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "certificates/properties/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceList<PropertyCategory> getActionTemplate(
			HttpServletRequest request, @PathVariable Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException, ResourceNotFoundException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			CertificateResource resource = resourceService.getResourceById(id);
			return new ResourceList<PropertyCategory>(
					resourceService.getPropertyTemplate(resource));
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "certificates/certificate/{id}", method = RequestMethod.GET, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public CertificateResource getResource(HttpServletRequest request,
			HttpServletResponse response, @PathVariable("id") Long id)
			throws AccessDeniedException, UnauthorizedException,
			ResourceNotFoundException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			return resourceService.getResourceById(id);
		} finally {
			clearAuthenticatedContext();
		}

	}

	@AuthenticationRequired
	@RequestMapping(value = "certificates/certificate", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<CertificateResource> createOrUpdateNetworkResource(
			HttpServletRequest request, HttpServletResponse response,
			@RequestBody ResourceUpdate resource) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			CertificateResource newResource;

			Realm realm = sessionUtils.getCurrentRealm(request);

			Map<String, String> properties = new HashMap<String, String>();
			for (PropertyItem i : resource.getProperties()) {
				properties.put(i.getId(), i.getValue());
			}

			if (resource.getId() != null) {
				newResource = resourceService.updateResource(
						resourceService.getResourceById(resource.getId()),
						resource.getName(), properties);
			} else {
				newResource = resourceService.createResource(
						resource.getName(), realm, properties, false);
			}
			return new ResourceStatus<CertificateResource>(newResource,
					I18N.getResource(sessionUtils.getLocale(request),
							CertificateResourceServiceImpl.RESOURCE_BUNDLE,
							resource.getId() != null ? "resource.updated.info"
									: "resource.created.info", resource
									.getName()));

		} catch (ResourceException e) {
			return new ResourceStatus<CertificateResource>(false,
					e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@SuppressWarnings("unchecked")
	@AuthenticationRequired
	@RequestMapping(value = "certificates/certificate/{id}", method = RequestMethod.DELETE, produces = { "application/json" })
	@ResponseBody
	@ResponseStatus(value = HttpStatus.OK)
	public ResourceStatus<CertificateResource> deleteResource(
			HttpServletRequest request, HttpServletResponse response,
			@PathVariable("id") Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {

			CertificateResource resource = resourceService.getResourceById(id);

			if (resource == null) {
				return new ResourceStatus<CertificateResource>(false,
						I18N.getResource(sessionUtils.getLocale(request),
								CertificateResourceServiceImpl.RESOURCE_BUNDLE,
								"error.invalidResourceId", id));
			}

			String preDeletedName = resource.getName();
			resourceService.deleteResource(resource);

			return new ResourceStatus<CertificateResource>(true,
					I18N.getResource(sessionUtils.getLocale(request),
							CertificateResourceServiceImpl.RESOURCE_BUNDLE,
							"resource.deleted.info", preDeletedName));

		} catch (ResourceException e) {
			return new ResourceStatus<CertificateResource>(false,
					e.getMessage());
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "certificates/downloadCSR/{id}", method = RequestMethod.GET, produces = { "text/plain" })
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	public String generateCSR(HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			CertificateStatus status = new CertificateStatus();
			status.setSuccess(false);
			try {

				CertificateResource resource = resourceService
						.getResourceById(id);
				String csr = resourceService.generateCSR(resource);
				response.setHeader("Content-Disposition",
						"attachment; filename=\"" + resource.getName()
								+ ".csr\"");
				return csr;
			} catch (Exception e) {
				try {
					response.sendError(HttpStatus.INTERNAL_SERVER_ERROR
							.ordinal());
				} catch (IOException e1) {
				}
				return null;
			}

		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "certificates/downloadCertificate/{id}", method = RequestMethod.GET, produces = { "text/plain" })
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	public String downloadCertificate(HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			CertificateStatus status = new CertificateStatus();
			status.setSuccess(false);
			try {

				CertificateResource resource = resourceService
						.getResourceById(id);
				String csr = resource.getCertificate();
				response.setHeader(
						"Content-Disposition",
						"attachment; filename=\""
								+ resource.getName().replace(' ', '_')
								+ ".crt\"");
				return csr;
			} catch (Exception e) {
				try {
					response.sendError(HttpStatus.INTERNAL_SERVER_ERROR
							.ordinal());
				} catch (IOException e1) {
				}
				return null;
			}

		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "certificates/exportPfx/{id}", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	public RequestStatus exportPfx(HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long id,
			@RequestParam(value = "passphrase") String passphrase)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			CertificateStatus status = new CertificateStatus();
			status.setSuccess(false);
			try {

				CertificateResource resource = resourceService
						.getResourceById(id);

				List<X509Certificate> certChain = new ArrayList<X509Certificate>();
				X509Certificate[] arr = X509CertificateUtils
						.loadCertificateChainFromPEM(new ByteArrayInputStream(
								resource.getCertificate().getBytes("UTF-8")));
				for (X509Certificate cert : arr) {
					certChain.add(cert);
				}
				if (StringUtils.isNotEmpty(resource.getBundle()))
					arr = X509CertificateUtils
							.loadCertificateChainFromPEM(new ByteArrayInputStream(
									resource.getBundle().getBytes("UTF-8")));
				for (X509Certificate cert : arr) {
					certChain.add(cert);
				}

				KeyStore keystore = X509CertificateUtils.createPKCS12Keystore(
						X509CertificateUtils.loadKeyPairFromPEM(
								new ByteArrayInputStream(resource
										.getPrivateKey().getBytes("UTF-8")),
								passphrase.toCharArray()), certChain
								.toArray(new X509Certificate[0]),
						"hypersocket", passphrase.toCharArray());

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				keystore.store(out, passphrase.toCharArray());
				request.getSession().setAttribute("pfx", out.toByteArray());
				return new RequestStatus(true);
			} catch (Exception e) {
				return new RequestStatus(false, e.getMessage());
			}

		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "certificates/downloadPfx/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	public byte[] downloadPfx(HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException, ResourceNotFoundException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			response.setHeader(
					"Content-Disposition",
					"attachment; filename=\""
							+ URLEncoder.encode(
									resourceService.getResourceById(id)
											.getName().replace(' ', '_'),
									"UTF-8") + ".pfx\"");
			return (byte[]) request.getSession().getAttribute("pfx");
		} catch (Exception e) {
			try {
				response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.ordinal());
			} catch (IOException e1) {
			}
			return null;
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "certificates/exportPem/{id}", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	public RequestStatus exportPem(HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long id,
			@RequestParam(value = "passphrase") String passphrase)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			CertificateStatus status = new CertificateStatus();
			status.setSuccess(false);
			try {

				CertificateResource resource = resourceService
						.getResourceById(id);

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				ZipOutputStream zip = new ZipOutputStream(
						new BufferedOutputStream(out));

				zip.putNextEntry(new ZipEntry("certificate.pem"));
				zip.write(resource.getCertificate().getBytes("UTF-8"));
				zip.closeEntry();

				if (StringUtils.isNotEmpty(resource.getBundle())) {
					zip.putNextEntry(new ZipEntry("ca-bundle.pem"));
					zip.write(resource.getBundle().getBytes("UTF-8"));
					zip.closeEntry();
				}
				zip.putNextEntry(new ZipEntry("key.pem"));
				if (passphrase == null || passphrase.trim().equals("")) {
					zip.write(resource.getPrivateKey().getBytes("UTF-8"));
				} else {
					KeyPair keypair = X509CertificateUtils.loadKeyPairFromPEM(
							new ByteArrayInputStream(resource.getPrivateKey()
									.getBytes("UTF-8")), passphrase
									.toCharArray());
					ByteArrayOutputStream bout = new ByteArrayOutputStream();
					JcaPEMWriter pem = new JcaPEMWriter(
							new OutputStreamWriter(bout));
					pem.writeObject(
							keypair,
							new JcePEMEncryptorBuilder("AES-128-CBC")
									.setProvider("BC").build(
											passphrase.toCharArray()));
					pem.flush();
					zip.write(bout.toByteArray());
					pem.close();
					bout.close();
				}
				zip.closeEntry();
				zip.close();
				request.getSession().setAttribute("pem", out.toByteArray());
				return new RequestStatus(true);
			} catch (Exception e) {
				return new RequestStatus(false, e.getMessage());
			}
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "certificates/downloadPem/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	public byte[] downloadPem(HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long id)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException, ResourceNotFoundException {
		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));
		try {
			response.setHeader(
					"Content-Disposition",
					"attachment; filename=\""
							+ URLEncoder.encode(
									resourceService.getResourceById(id)
											.getName().replace(' ', '_'),
									"UTF-8") + ".zip\"");
			return (byte[]) request.getSession().getAttribute("pem");
		} catch (Exception e) {
			try {
				response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.ordinal());
			} catch (IOException e1) {
			}
			return null;
		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "certificates/cert/{id}", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	public CertificateStatus uploadCertificate(HttpServletRequest request,
			HttpServletResponse response, @PathVariable Long id,
			@RequestPart(value = "file") MultipartFile file,
			@RequestPart(value = "bundle") MultipartFile bundle)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			CertificateStatus status = new CertificateStatus();
			status.setSuccess(false);
			try {
				CertificateResource resource = resourceService
						.getResourceById(id);
				resourceService.updateCertificate(resource, file, bundle);
				status.setSuccess(true);
				status.setMessage(I18N.getResource(
						sessionUtils.getLocale(request),
						CertificateResourceServiceImpl.RESOURCE_BUNDLE,
						"info.certUploaded"));

			} catch (Exception ex) {
				status.setMessage(I18N.getResource(
						sessionUtils.getLocale(request),
						CertificateResourceServiceImpl.RESOURCE_BUNDLE,
						"error.generalError", ex.getMessage()));
			}

			return status;

		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "certificates/pem", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	public CertificateStatus uploadKey(HttpServletRequest request,
			HttpServletResponse response,
			@RequestPart(value = "file") MultipartFile file,
			@RequestPart(value = "bundle") MultipartFile bundle,
			@RequestPart(value = "key") MultipartFile key,
			@RequestParam(value = "passphrase") String passphrase)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {

		return replaceKey(request, response, file, bundle, key, passphrase,
				null);
	}

	@AuthenticationRequired
	@RequestMapping(value = "certificates/pem/{id}", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	public CertificateStatus replaceKey(HttpServletRequest request,
			HttpServletResponse response,
			@RequestPart(value = "file") MultipartFile file,
			@RequestPart(value = "bundle") MultipartFile bundle,
			@RequestPart(value = "key") MultipartFile key,
			@RequestParam(value = "passphrase") String passphrase,
			@PathVariable Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			CertificateStatus status = new CertificateStatus();
			status.setSuccess(false);
			try {

				if (id == null) {
					status.setResource(resourceService.importPrivateKey(key,
							passphrase, file, bundle));
				} else {
					status.setResource(resourceService.replacePrivateKey(
							resourceService.getResourceById(id), key,
							passphrase, file, bundle));
				}
				status.setSuccess(true);
				status.setMessage(I18N.getResource(
						sessionUtils.getLocale(request),
						CertificateResourceServiceImpl.RESOURCE_BUNDLE,
						"info.keyUploaded"));
			} catch (InvalidPassphraseException e) {
				status.setMessage(I18N.getResource(
						sessionUtils.getLocale(request),
						CertificateResourceServiceImpl.RESOURCE_BUNDLE,
						"error.invalidPassphrase"));
			} catch (Exception e) {
				status.setMessage(I18N.getResource(
						sessionUtils.getLocale(request),
						CertificateResourceServiceImpl.RESOURCE_BUNDLE,
						"error.generalError", e.getMessage()));
			}

			return status;

		} finally {
			clearAuthenticatedContext();
		}
	}

	@AuthenticationRequired
	@RequestMapping(value = "certificates/pfx", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	public CertificateStatus uploadPfx(HttpServletRequest request,
			HttpServletResponse response,
			@RequestPart(value = "key") MultipartFile key,
			@RequestParam(value = "passphrase") String passphrase)
			throws AccessDeniedException, UnauthorizedException,
			SessionTimeoutException {
		return replacePfx(request, response, key, passphrase, null);
	}

	@AuthenticationRequired
	@RequestMapping(value = "certificates/pfx/{id}", method = RequestMethod.POST, produces = { "application/json" })
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	public CertificateStatus replacePfx(HttpServletRequest request,
			HttpServletResponse response,
			@RequestPart(value = "key") MultipartFile key,
			@RequestParam(value = "passphrase") String passphrase,
			@PathVariable Long id) throws AccessDeniedException,
			UnauthorizedException, SessionTimeoutException {

		setupAuthenticatedContext(sessionUtils.getSession(request),
				sessionUtils.getLocale(request));

		try {
			CertificateStatus status = new CertificateStatus();
			status.setSuccess(false);
			try {
				if (id == null) {
					status.setResource(resourceService.importPfx(key,
							passphrase));
				} else {
					CertificateResource resource = resourceService
							.getResourceById(id);
					status.setResource(resourceService.replacePfx(resource,
							key, passphrase));
				}
				status.setSuccess(true);
				status.setMessage(I18N.getResource(
						sessionUtils.getLocale(request),
						CertificateResourceServiceImpl.RESOURCE_BUNDLE,
						"info.keyUploaded"));
				// } catch (InvalidPassphraseException e) {
				// status.setMessage(I18N.getResource(
				// sessionUtils.getLocale(request),
				// CertificateService.RESOURCE_BUNDLE,
				// "error.invalidPassphrase"));
			} catch (Exception e) {
				status.setMessage(I18N.getResource(
						sessionUtils.getLocale(request),
						CertificateResourceServiceImpl.RESOURCE_BUNDLE,
						"error.generalError", e.getMessage()));
			}

			return status;

		} finally {
			clearAuthenticatedContext();
		}
	}
}
