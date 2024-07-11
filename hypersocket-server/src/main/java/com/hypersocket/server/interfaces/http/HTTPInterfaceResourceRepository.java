package com.hypersocket.server.interfaces.http;

import java.util.Collection;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceRepository;

public interface HTTPInterfaceResourceRepository extends
		AbstractResourceRepository<HTTPInterfaceResource> {

	Collection<HTTPInterfaceResource> allInterfaces();
	
	Collection<HTTPInterfaceResource> getHTTPInterfaceResourceByProtocol(Realm realm, HTTPProtocol protocol);

	Collection<HTTPInterfaceResource> getHTTPInterfaceResourcesWithSameCertificate(Realm realm, Long id);

}
