package com.hypersocket.upload;

import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.repository.CriteriaConfiguration;
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
	
	@Override
	@Transactional(readOnly=true)
	public FileUpload getFileByShortCode(String shortCode) {
		return get("shortCode", shortCode, FileUpload.class);
	}
	
	@Override
	@Transactional(readOnly=true)
	public FileUpload getFileUpload(final String uuidOrShortCode) {
		return get(FileUpload.class, new CriteriaConfiguration() {
			
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.or(Restrictions.eq("name", uuidOrShortCode),
						Restrictions.eq("shortCode", uuidOrShortCode)));
			}
		});
	}
	
	@Override
	@Transactional(readOnly=true)
	public Collection<FileUpload> allResources() {
		return list(FileUpload.class);
	}
	
	

}
