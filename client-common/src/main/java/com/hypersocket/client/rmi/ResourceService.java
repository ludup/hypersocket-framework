package com.hypersocket.client.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ResourceService extends Remote {

	public List<ResourceRealm> getResourceRealms() throws RemoteException;

	ResourceRealm getResourceRealm(String name) throws RemoteException;

	public void removeResourceRealm(String host) throws RemoteException;
	
}
