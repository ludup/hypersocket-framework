package com.hypersocket.upload;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;

public interface FileUploadService extends AbstractResourceService<FileUpload> {

	public FileUpload createFile(MultipartFile file, Realm realm, String type)
			throws ResourceException, AccessDeniedException,
			IOException;

//	public FileUpload getFileByUuid(String uuid) throws ResourceNotFoundException;

	public void deleteFile(FileUpload fileUpload)
			throws ResourceException, AccessDeniedException;

	FileUpload createFile(MultipartFile file, Realm realm, boolean persist,
			String type,
			FileStore uploadStore) throws ResourceException,
			AccessDeniedException, IOException;

	FileUpload createFile(InputStream file, String filename, Realm realm,
			boolean persist, String type, FileStore uploadStore)
			throws ResourceException, AccessDeniedException,
			IOException;

	void downloadURIFile(String uuid, HttpServletRequest request,
			HttpServletResponse response, boolean forceDownload)
			throws IOException, AccessDeniedException, ResourceNotFoundException;

	String getContentType(String uuid) throws ResourceNotFoundException,
			IOException;

	public String getContentType(File file);

	FileUpload createFile(File outputFile, String filename, Realm realm, boolean persist, String type)
			throws ResourceException, AccessDeniedException, IOException;

	FileStore getDefaultStore();

	void setDefaultStore(FileStore defaultStore);

	String getContentType(String uuid, boolean isUUID) throws ResourceNotFoundException, IOException;

	String getFilenameContentType(String filename) throws ResourceNotFoundException, IOException;
	
	FileUpload createFile(InputStream in, String filename, Realm realm, boolean persist)
			throws ResourceException, AccessDeniedException, IOException;

	public InputStream getInputStream(String uuid) throws IOException;

	File createTempFile(String uuid) throws IOException;

	FileUpload getFileUpload(String uuid) throws ResourceNotFoundException;

	FileUpload copyFile(String uuid)
			throws ResourceNotFoundException, ResourceException, AccessDeniedException, IOException;
}
