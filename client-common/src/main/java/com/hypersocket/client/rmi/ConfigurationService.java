package com.hypersocket.client.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ConfigurationService extends Remote {

	String getValue(String name, String defaultValue) throws RemoteException;
	
	void setValue(String name, String value) throws RemoteException; 
	
	List<ConfigurationItem> getConfigurationItems() throws RemoteException;
}
