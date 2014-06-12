package com.hypersocket.service;

import com.hypersocket.auth.AuthenticatedService;

public interface ServiceManagementService extends AuthenticatedService {

	void registerService(ManageableService service);

}
