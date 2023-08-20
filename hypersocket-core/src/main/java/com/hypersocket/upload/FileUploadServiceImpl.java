package com.hypersocket.upload;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.cache.Cache;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.ConfigurableMimeFileTypeMap;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hypersocket.cache.CacheService;
import com.hypersocket.cache.CacheUtils;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.util.CloseOnEOFInputStream;
import com.hypersocket.utils.HypersocketUtils;

@Service
public class FileUploadServiceImpl extends
		AbstractResourceServiceImpl<FileUpload> implements FileUploadService {

	public static final String RESOURCE_BUNDLE = "FileUploadService";
	public static final String DEFAULT_UPLOAD_PATH = "conf/uploads/";
	
	static ConfigurableMimeFileTypeMap mimeTypesMap = new ConfigurableMimeFileTypeMap();

	static Logger log = LoggerFactory.getLogger(FileUploadServiceImpl.class);
	
	public static final String CONTENT_INPUTSTREAM = "ContentInputStream";
	
	@Autowired
	private FileUploadRepository repository;

	@Autowired
	private I18NService i18nService;

	@Autowired
	private CacheService cacheService;
	
	private FileStore defaultStore = new DefaultFileStore();
	private Cache<String, FileUpload> cache;


	public FileUploadServiceImpl() {
		super("fileUploads");
	}
	
	@PostConstruct
	private void postConstruct() {
		cache = cacheService.getCacheOrCreate("fileUploads", String.class, FileUpload.class);
		i18nService.registerBundle(RESOURCE_BUNDLE);
		EntityResourcePropertyStore.registerResourceService(FileUpload.class, repository);
		setAssertPermissions(false);
	}
	
	@Override
	public FileStore getDefaultStore() {
		return defaultStore;
	}
	
	@Override
	public void setDefaultStore(FileStore defaultStore) {
		this.defaultStore = defaultStore;
	}
	
	@Override
	public String getContentType(String uuid) throws ResourceNotFoundException, IOException {
		return getContentType(uuid, true);
	}
	
	@Override
	public String getFilenameContentType(String filename) throws ResourceNotFoundException, IOException {
		return getContentType(filename, false);
	} 
	
	@Override
	public String getContentType(String uuid, boolean isUUID) throws ResourceNotFoundException, IOException {
		if(isUUID) {
			FileUpload upload = getFileUpload(uuid);
			String contentType = mimeTypesMap.getContentType(upload.getFileName());
			if(contentType==null) {
				return "application/octet-stream";
			}
			return contentType;
		} else {
			String contentType = mimeTypesMap.getContentType(uuid);
			if(contentType==null) {
				return "application/octet-stream";
			}
			return contentType;
		}
	}
	
	@Override
	public FileUpload createFile(InputStream file, String filename, Realm realm,
			boolean publicFile, String contentType)
			throws ResourceException, AccessDeniedException,
			IOException {
		return createFile(file, filename, realm, getDefaultStore(), publicFile, contentType);
	}

	@Override
	public FileUpload createFile(InputStream in, String filename, String contentType, Realm realm, boolean publicFile)
			throws ResourceException, AccessDeniedException, IOException {
		return createFile(in, filename, realm, getDefaultStore(), publicFile, contentType);
	}
	@Override
	public FileUpload createFile(final MultipartFile file, final Realm realm, boolean publicFile)
			throws ResourceException, AccessDeniedException,
			IOException {

		return createFile(file, realm, getDefaultStore(), publicFile);
	}

	@Override
	public FileUpload createFile(MultipartFile file, Realm realm,
			FileStore uploadStore, boolean publicFile)
			throws ResourceException, AccessDeniedException,
			IOException {

		return createFile(file.getInputStream(), file.getOriginalFilename(),
				realm, uploadStore, publicFile);
	}

	@Override
	public FileUpload createFile(InputStream in, String filename, Realm realm, boolean publicFile)
			throws ResourceException, AccessDeniedException,
			IOException {
		return createFile(in, filename, realm, defaultStore, publicFile);
	}
	@Override
	public FileUpload createFile(InputStream in, String filename, Realm realm,
			FileStore uploadStore, boolean publicFile)
			throws ResourceException, AccessDeniedException,
			IOException {
		return createFile(in, filename, realm, uploadStore, publicFile, mimeTypesMap.getContentType(filename));
	}
	@Override
	public FileUpload createFile(InputStream in, String filename, Realm realm,
			FileStore uploadStore, boolean publicFile, String contentType)
			throws ResourceException, AccessDeniedException,
			IOException {

		FileUpload fileUpload = new FileUpload();
		fileUpload.setFileName(filename);
		fileUpload.setRealm(realm);
		fileUpload.setName(fileUpload.getUUID());
		fileUpload.setType(contentType);
		fileUpload.setPublicFile(publicFile);
		
		String shortCode;
		do {
			shortCode = HypersocketUtils.generateRandomAlphaNumericString(6);
		} while(repository.getFileByShortCode(shortCode)!=null);
		
		fileUpload.setShortCode(shortCode);
		
		try {

			MessageDigest md5 = MessageDigest.getInstance("MD5");
			DigestInputStream din = null;
			OutputStream out = null;

			try {

				din = new DigestInputStream(in, md5);

				fileUpload.setFileSize(uploadStore.upload(String.format("%d/%s", 
						fileUpload.getRealm().getId(), fileUpload.getName()), din));

				String md5String = Hex.encodeHexString(md5.digest());
				fileUpload.setMd5Sum(md5String);
			} finally {
				IOUtils.closeQuietly(out);
				IOUtils.closeQuietly(din);
				IOUtils.closeQuietly(in);
			}

			createResource(fileUpload, new HashMap<String, String>());

			return fileUpload;
		} catch (Throwable e) {
			fireResourceCreationEvent(fileUpload, e);
			throw new ResourceCreationException(e, 
					FileUploadServiceImpl.RESOURCE_BUNDLE,
					"error.genericError", e.getMessage());
		}

	}

	@Override
	public FileUpload getFileUpload(String uuid) throws ResourceNotFoundException {
		var upload = (FileUpload)cacheService.getOrGet(cache, "uuid_" + uuid, () -> {
			return repository.getFileUpload(uuid);
		});
		if(upload==null) {
			throw new ResourceNotFoundException(RESOURCE_BUNDLE, "error.fileNotFound", uuid);
		}
		return upload;
	}

	@Override
	public void deleteFile(FileUpload fileUpload)
			throws ResourceException, AccessDeniedException {
		
		try {
			cache.remove("uuid_" + fileUpload.getUUID());
			defaultStore.delete(String.format("%d/%s", 
					fileUpload.getRealm().getId(), fileUpload.getName()));
			
			deleteResource(fileUpload);
		} catch (IOException  e) { 
			fireResourceDeletionEvent(fileUpload, e);
			throw new ResourceException(e);
		} catch(ResourceException | AccessDeniedException e) {
			fireResourceDeletionEvent(fileUpload, e);
			throw e;
		}

	}

	@Override
	public void downloadURIFile(String uuid, HttpServletRequest request, HttpServletResponse response, boolean forceDownload, boolean requirePublic, boolean checkCache)
			throws IOException, AccessDeniedException, ResourceNotFoundException {

		FileUpload fileUpload = getFileUpload(uuid);

		if(!hasSessionContext() && requirePublic && !fileUpload.getPublicFile()) {
			throw new AccessDeniedException();
		}
		
		if(checkCache && CacheUtils.checkValidCache(request, response, fileUpload.getModifiedDate().getTime())) {
			return;
		}
		
		InputStream in = getInputStream(uuid);
		
		String contentType = fileUpload.getType();
		
		if (StringUtils.isBlank(contentType)) {
			contentType = mimeTypesMap.getContentType(fileUpload.getFileName());
		}

		if(log.isDebugEnabled()) {
			log.debug(String.format("Setting Content-Type of request to %s", contentType));
		}
		
		response.setContentType(contentType);
		response.setStatus(HttpStatus.SC_OK);
		response.setContentLength(fileUpload.getFileSize().intValue());
		
		if(forceDownload) {
			response.setHeader("Content-disposition", "attachment; filename="
				+ fileUpload.getFileName());
		}

		// Let the HTTP server handle it.
		request.setAttribute(CONTENT_INPUTSTREAM, in);
		CacheUtils.setDateAndCacheHeaders(response, fileUpload.getModifiedDate().getTime(), true, request.getRequestURI());
	}
	
	@Override
	public File createTempFile(String uuid) throws IOException {
		File file = File.createTempFile("task", "template");
		file.deleteOnExit();
		FileOutputStream out = new FileOutputStream(file);
		try {
			IOUtils.copy(new CloseOnEOFInputStream(getInputStream(uuid)), out);
		} finally {
			IOUtils.closeQuietly(out);
		}
		return file;
	}

	@Override
	protected AbstractResourceRepository<FileUpload> getRepository() {
		return repository;
	}

	@Override
	protected String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public Class<? extends PermissionType> getPermissionType() {
		return FileUploadPermission.class;
	}
	
	protected Class<FileUpload> getResourceClass() {
		return FileUpload.class;
	}
	
	@Override
	protected void fireResourceCreationEvent(FileUpload resource) {
	}

	@Override
	protected void fireResourceCreationEvent(FileUpload resource, Throwable t) {
	}

	@Override
	protected void fireResourceUpdateEvent(FileUpload resource) {
	}

	@Override
	protected void fireResourceUpdateEvent(FileUpload resource, Throwable t) {
	}

	@Override
	protected void fireResourceDeletionEvent(FileUpload resource) {
	}

	@Override
	protected void fireResourceDeletionEvent(FileUpload resource, Throwable t) {
	}

	@Override
	public FileUpload createFile(File outputFile, String filename,
			Realm realm, String type, boolean publicFile) throws ResourceException, AccessDeniedException, IOException {
		
		InputStream in = new FileInputStream(outputFile);
		try {
			return createFile(in, filename, realm, getDefaultStore(), publicFile);
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	class DefaultFileStore implements FileStore {
		
		DefaultFileStore() {
		}
		
		public long upload(String path, InputStream in)
				throws IOException {

			File f = new File(
					System.getProperty("hypersocket.uploadPath", DEFAULT_UPLOAD_PATH)
					+ path);
			f.getParentFile().mkdirs();
			f.createNewFile();

			OutputStream out = new FileOutputStream(f);

			try {
				IOUtils.copyLarge(in, out);
			} finally {
				IOUtils.closeQuietly(out);
				IOUtils.closeQuietly(in);
			}

			return f.length();
		}

		@Override
		public InputStream getInputStream(String path) throws IOException {
			
			File f = new File(
					System.getProperty("hypersocket.uploadPath", DEFAULT_UPLOAD_PATH)
					+ path);
			return new FileInputStream(f);
		}

		@Override
		public OutputStream getOutputStream(String path) throws IOException {
			File f = new File(
					System.getProperty("hypersocket.uploadPath", DEFAULT_UPLOAD_PATH)
					+ path);
			f.getParentFile().mkdirs();
			return new FileOutputStream(f);
		}

		@Override
		public void delete(String path) throws IOException {
			
			File f = new File(
					System.getProperty("hypersocket.uploadPath", DEFAULT_UPLOAD_PATH)
					+ path);
			
			f.delete();
		}
	}

	@Override
	public String getContentType(File file) {
		String contentType = mimeTypesMap.getContentType(file);
		if(contentType==null) {
			return "application/octet-stream";
		}
		return contentType;
	}

	@Override
	public InputStream getInputStream(String uuid) throws IOException {
		try {
			FileUpload upload = getFileUpload(uuid);
			return defaultStore.getInputStream((String.format("%d/%s", upload.getRealm().getId(), upload.getName())));
		} catch (ResourceNotFoundException e) {
			throw new IOException(e.getMessage(), e);
		}
	}
	
	@Override
	public FileUpload copyFile(String uuid) throws ResourceNotFoundException, ResourceException, AccessDeniedException, IOException {
		
		FileUpload u = getFileUpload(uuid);
		return createFile(getInputStream(u.getName()), 
				u.getFileName(), u.getRealm(), u.getPublicFile());
	}

	@Override
	protected void prepareExport(FileUpload resource) throws ResourceException, AccessDeniedException {
		super.prepareExport(resource);
		
		InputStream in = null;
		Base64InputStream b = null;
		try {
			in = getInputStream(resource.getName());
			b = new Base64InputStream(in, true);

			resource.setContent(IOUtils.toString(b));
					
		} catch(IOException ex) {
			throw new ResourceException(RESOURCE_BUNDLE, "error.fileIO", ex.getMessage());
		} finally {
			IOUtils.closeQuietly(b);
			IOUtils.closeQuietly(in);
		}
	}

	protected void beforeCreateResource(FileUpload resource, Map<String,String> properties) throws ResourceException {
		if(resource.getContent()!=null) {
			
			FileOutputStream out = null;
			Base64InputStream b = null;
			
			try {
				b = new Base64InputStream(IOUtils.toInputStream(resource.getContent(), "UTF-8"), false);
				defaultStore.upload(String.format("%d/%s", resource.getRealm().getId(), resource.getName()), b);
				 
			} catch(IOException ex) {
				throw new ResourceCreationException(RESOURCE_BUNDLE, "error.fileIO", ex.getMessage());
			} finally {
				IOUtils.closeQuietly(out);
				IOUtils.closeQuietly(b);
			}
		}

	}

}
