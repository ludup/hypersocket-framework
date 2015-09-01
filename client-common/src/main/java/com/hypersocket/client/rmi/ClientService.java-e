package com.hypersocket.client.rmi;

import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ClientService extends Remote {
	
	boolean isGUINeedsUpdating() throws RemoteException;
	
	boolean isUpdating() throws RemoteException;

	void registerGUI(GUICallback gui) throws RemoteException;

	void unregisterGUI(GUICallback gui) throws RemoteException;
	
	boolean isConnected(Connection c) throws RemoteException;
	
	Connection save(Connection c) throws RemoteException;
	
	void connect(Connection c) throws RemoteException;
	
	void disconnect(Connection c) throws RemoteException;

	List<ConnectionStatus> getStatus() throws RemoteException;
	
	byte[] getBlob(String host, String path, long timeout) throws RemoteException;

	void ping() throws RemoteException;
	
	ConnectionService getConnectionService() throws RemoteException;
	
	ConfigurationService getConfigurationService() throws RemoteException;

	int getStatus(Connection con) throws RemoteException;

	void scheduleConnect(Connection c) throws RemoteException;

	byte[] getBlob(Connection connection, String path, long timeout)
			throws IOException, RemoteException;

//	void maybeUpdate(Connection c) throws RemoteException;
}
