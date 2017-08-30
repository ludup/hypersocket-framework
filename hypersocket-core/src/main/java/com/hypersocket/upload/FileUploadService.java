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

	public FileUpload createFile(MultipartFile file, Realm realm, String type, boolean publicFile)
			throws ResourceException, AccessDeniedException,
			IOException;

	FileUpload createFile(MultipartFile file, Realm realm, 
			String type,
			FileStore uploadStore, boolean publicFile) throws ResourceException,
			AccessDeniedException, IOException;

	FileUpload createFile(InputStream file, String filename, Realm realm,
			 String type, FileStore uploadStore, boolean publicFile)
			throws ResourceException, AccessDeniedException,
			IOException;

	FileUpload createFile(File outputFile, String filename, Realm realm,  String type, boolean publicFile)
			throws ResourceException, AccessDeniedException, IOException;

	FileUpload createFile(InputStream in, String filename, Realm realm, boolean publicFile)
			throws ResourceException, AccessDeniedException, IOException;
	
	public void deleteFile(FileUpload fileUpload)
			throws ResourceException, AccessDeniedException;
	
	void downloadURIFile(String uuid, HttpServletRequest request,
			HttpServletResponse response, boolean forceDownload, boolean requirePublic)
			throws IOException, AccessDeniedException, ResourceNotFoundException;

	String getContentType(String uuid) throws ResourceNotFoundException,
			IOException;

	public String getContentType(File file);

	FileStore getDefaultStore();

	void setDefaultStore(FileStore defaultStore);

	String getContentType(String uuid, boolean isUUID) throws ResourceNotFoundException, IOException;

	String getFilenameContentType(String filename) throws ResourceNotFoundException, IOException;

	public InputStream getInputStream(String uuid) throws IOException;

	File createTempFile(String uuid) throws IOException;

	FileUpload getFileUpload(String uuid) throws ResourceNotFoundException;

	FileUpload copyFile(String uuid)
			throws ResourceNotFoundException, ResourceException, AccessDeniedException, IOException;
}
