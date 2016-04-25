package com.hypersocket.server.interfaces.http;

import java.util.Collection;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.resource.AbstractResourceRepositoryImpl;

@Repository
public class HTTPInterfaceResourceRepositoryImpl extends
		AbstractResourceRepositoryImpl<HTTPInterfaceResource> implements
		HTTPInterfaceResourceRepository {

	@Override
	protected Class<HTTPInterfaceResource> getResourceClass() {
		return HTTPInterfaceResource.class;
	}

	@Override
	@Transactional(readOnly=true)
	public Collection<HTTPInterfaceResource> allInterfaces() {
		return allEntities(HTTPInterfaceResource.class);
	}

}
