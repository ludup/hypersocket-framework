package com.hypersocket.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Service;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.realm.Realm;

@Service
public class ServiceManagementServiceImpl extends AbstractAuthenticatedServiceImpl implements ServiceManagementService {

	List<ManageableService> systemServices = new ArrayList<ManageableService>();
	List<ManageableService> realmServices = new ArrayList<ManageableService>();
	
	@Override
	public void registerService(ManageableService service) {
		if(service.isSystem()) {
			systemServices.add(service);
		} else {
			realmServices.add(service);
		}
	}
	
	@Override
	public Collection<ManageableService> getServices(Realm realm) {
		if(realm.isSystem() || realm.isDefaultRealm()) {
			return Collections.unmodifiableCollection(systemServices);
		} else {
			return Collections.unmodifiableCollection(realmServices);
		}
	}
}
