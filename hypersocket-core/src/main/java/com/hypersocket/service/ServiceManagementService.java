package com.hypersocket.service;

import java.util.Collection;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.realm.Realm;

public interface ServiceManagementService extends AuthenticatedService {

	void registerService(ManageableService service);

	Collection<ManageableService> getServices(Realm realm);

}
