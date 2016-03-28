package com.hypersocket.server.interfaces.http;

import java.util.Collection;

import com.hypersocket.resource.AbstractResourceRepository;

public interface HTTPInterfaceResourceRepository extends
		AbstractResourceRepository<HTTPInterfaceResource> {

	Collection<HTTPInterfaceResource> allInterfaces();

}
