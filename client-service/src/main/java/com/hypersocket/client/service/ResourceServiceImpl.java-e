package com.hypersocket.client.service;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.hypersocket.client.ServiceResource;
import com.hypersocket.client.rmi.Resource;
import com.hypersocket.client.rmi.ResourceRealm;
import com.hypersocket.client.rmi.ResourceRealmImpl;
import com.hypersocket.client.rmi.ResourceService;

public class ResourceServiceImpl implements ResourceService {

	Map<String, ResourceRealm> resourceRealms = new HashMap<String,ResourceRealm>();
	List<ServiceResource> serviceResources = new ArrayList<ServiceResource>();
	
	public ResourceServiceImpl() {
	}

	@Override
	public ResourceRealm getResourceRealm(String name) throws RemoteException {
		if(!resourceRealms.containsKey(name)) {
			resourceRealms.put(name, new ResourceRealmImpl(name, new ArrayList<Resource>()));
		}
		return resourceRealms.get(name);
	}

	@Override
	public List<ResourceRealm> getResourceRealms() throws RemoteException {
		return new ArrayList<ResourceRealm>(resourceRealms.values());
	}

	@Override
	public void removeResourceRealm(String name) throws RemoteException {
		resourceRealms.remove(name);
	}

	@Override
	public List<ServiceResource> getServiceResources() throws RemoteException {
		return serviceResources;
	}

}
