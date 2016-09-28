package com.hypersocket.upload;

import java.util.Collection;

import com.hypersocket.resource.AbstractResourceRepository;

public interface FileUploadRepository extends
		AbstractResourceRepository<FileUpload> {

	public FileUpload getFileByUuid(String uuid);

	FileUpload getFileByShortCode(String shortCode);

	FileUpload getFileUpload(String uuidOrShortCode);

	Collection<FileUpload> allResources();


}
