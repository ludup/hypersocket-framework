package com.hypersocket.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;

@Service
public class ServiceManagementServiceImpl extends AbstractAuthenticatedServiceImpl implements ServiceManagementService {

	Map<String,ManageableService> services = new HashMap<String,ManageableService>();
	
	@Override
	public void registerService(ManageableService service) {
		services.put(service.getResourceKey(), service);
	}
	
	@Override
	public Collection<ManageableService> getServices() {
		return Collections.unmodifiableCollection(services.values());
	}
}
