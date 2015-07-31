package com.hypersocket.upload;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.multipart.MultipartFile;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceNotFoundException;

public interface FileUploadService extends AbstractResourceService<FileUpload> {

	public FileUpload createFile(MultipartFile file, Realm realm)
			throws ResourceCreationException, AccessDeniedException,
			IOException;

	public FileUpload getFileByUuid(String uuid) throws ResourceNotFoundException;

	public void deleteFile(FileUpload fileUpload)
			throws ResourceChangeException, AccessDeniedException;

	FileUpload createFile(MultipartFile file, Realm realm, boolean persist,
			FileUploadStore uploadStore) throws ResourceCreationException,
			AccessDeniedException, IOException;

	FileUpload createFile(InputStream file, String filename, Realm realm,
			boolean persist, FileUploadStore uploadStore)
			throws ResourceCreationException, AccessDeniedException,
			IOException;

	void downloadURIFile(String uuid, HttpServletRequest request,
			HttpServletResponse response, boolean forceDownload)
			throws IOException, AccessDeniedException, ResourceNotFoundException;

	FileUpload createFile(File outputFile, String string, Realm currentRealm,
			boolean b) throws FileNotFoundException, ResourceCreationException, AccessDeniedException, IOException;

	File getFile(String uuid) throws IOException, ResourceNotFoundException;

	String getContentType(String uuid) throws ResourceNotFoundException,
			IOException;

	public String getContentType(File file);
}
