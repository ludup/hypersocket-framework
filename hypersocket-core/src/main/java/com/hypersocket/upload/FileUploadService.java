package com.hypersocket.upload;

import java.io.IOException;

import org.springframework.web.multipart.MultipartFile;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;

public interface FileUploadService extends AbstractResourceService<FileUpload> {

	public FileUpload createFile(MultipartFile file, Realm realm)
			throws ResourceCreationException, AccessDeniedException,
			IOException;

	public FileUpload getFileByUuid(String uuid);

	public void deleteFile(FileUpload fileUpload)
			throws ResourceChangeException, AccessDeniedException;
}
