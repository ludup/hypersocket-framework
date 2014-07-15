package com.hypersocket.client.service;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.hypersocket.client.rmi.Resource;
import com.hypersocket.client.rmi.ResourceImpl;
import com.hypersocket.client.rmi.ResourceProtocol;
import com.hypersocket.client.rmi.ResourceProtocolImpl;
import com.hypersocket.client.rmi.ResourceRealm;
import com.hypersocket.client.rmi.ResourceRealmImpl;
import com.hypersocket.client.rmi.ResourceService;

public class ResourceServiceImpl implements ResourceService {

	public ResourceServiceImpl() {
	}

	@Override
	public List<ResourceRealm> getResourceRealms() throws RemoteException {
		
		List<ResourceRealm> realms = new ArrayList<ResourceRealm>();
		
		ResourceProtocol r1 = new ResourceProtocolImpl(0L, "SSH");
		ResourceProtocol r2 = new ResourceProtocolImpl(1L, "HTTP");
		
		Resource group1 = new ResourceImpl("jenkins", Arrays.asList(r1, r2));
		
		ResourceRealm realm1 = new ResourceRealmImpl("glade.shacknet.nu", Arrays.asList(group1));
		
		ResourceProtocol r3 = new ResourceProtocolImpl(3L, "HTTP");
		
		Resource group2 = new ResourceImpl("proxy", Arrays.asList(r3));
		
		ResourceRealm realm2 = new ResourceRealmImpl("proxy.5socket.net", Arrays.asList(group2));
		
		realms.add(realm1);
		realms.add(realm2);
		
		return realms;
	}

}
