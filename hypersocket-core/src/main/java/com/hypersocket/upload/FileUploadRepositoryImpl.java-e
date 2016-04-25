package com.hypersocket.upload;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.resource.AbstractResourceRepositoryImpl;

@Repository
public class FileUploadRepositoryImpl extends
		AbstractResourceRepositoryImpl<FileUpload> implements
		FileUploadRepository {

	@Override
	protected Class<FileUpload> getResourceClass() {
		return FileUpload.class;
	}

	@Override
	@Transactional(readOnly=true)
	public FileUpload getFileByUuid(String uuid) {
		return get("name", uuid, FileUpload.class);
	}

}
