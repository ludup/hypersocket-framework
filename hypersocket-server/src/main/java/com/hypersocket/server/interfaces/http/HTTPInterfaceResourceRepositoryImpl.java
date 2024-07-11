package com.hypersocket.server.interfaces.http;

import java.util.Collection;

import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;
import com.hypersocket.resource.RealmCriteria;

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

	@Override
	@Transactional(readOnly=true)
	public Collection<HTTPInterfaceResource> getHTTPInterfaceResourceByProtocol(Realm realm, HTTPProtocol protocol) {
		return list("protocol", protocol, HTTPInterfaceResource.class, new RealmCriteria(realm));
	}

	@Override
	@Transactional(readOnly=true)
	public Collection<HTTPInterfaceResource> getHTTPInterfaceResourcesWithSameCertificate(Realm realm, Long id) {
		var httpResource = getResourceById(id);
		
		if (httpResource == null) {
			throw new IllegalArgumentException(String.format("No HTTP Interface resource found for id %s", id));
		}
		
		var certificateResource = httpResource.getCertificate();
		
		if (certificateResource == null) {
			throw new IllegalArgumentException(String.format("No Certificate resource found for HTTP Interface id %s", id));
		}
		
		return list(HTTPInterfaceResource.class, (criteria) -> {
			criteria.add(Restrictions.not(Restrictions.eq("id", httpResource.getId())));
			criteria.add(Restrictions.eq("certificate", certificateResource));
			criteria.add(Restrictions.eq("realm", realm));
		});
	}

}
