package com.hypersocket.server.interfaces;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Repository;

import com.hypersocket.resource.AbstractResourceRepositoryImpl;

@Repository
public class InterfaceResourceRepositoryImpl extends AbstractResourceRepositoryImpl<InterfaceResource>
		implements InterfaceResourceRepository {

	
	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("interfaceResourceTemplate.xml");
	}

	@Override
	protected Class<InterfaceResource> getResourceClass() {
		return InterfaceResource.class;
	}

}
