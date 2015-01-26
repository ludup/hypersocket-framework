package com.hypersocket.certificates;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.resource.AbstractResourceRepositoryImpl;

@Repository
@Transactional
public class CertificateResourceRepositoryImpl extends
		AbstractResourceRepositoryImpl<CertificateResource> implements
		CertificateResourceRepository {

	@Override
	protected Class<CertificateResource> getResourceClass() {
		return CertificateResource.class;
	}

}
