package com.hypersocket.resource;

import org.springframework.stereotype.Repository;

import com.hypersocket.resource.AbstractResourceRepositoryImpl;

@Repository
public class ResourceGroupRepositoryImpl extends AbstractResourceRepositoryImpl<ResourceGroup> implements ResourceGroupRepository {

	@Override
	protected Class<ResourceGroup> getResourceClass() {
		return ResourceGroup.class;
	}


}
