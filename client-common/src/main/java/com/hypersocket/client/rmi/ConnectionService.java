package com.hypersocket.client.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface ConnectionService extends Remote {

	public Connection createNew() throws RemoteException;
	
	public Connection save(Connection connection) throws RemoteException;
	
	public List<Connection> getConnections() throws RemoteException;

	public void delete(Connection con) throws RemoteException;

	public Connection getConnection(Long id) throws RemoteException;

	Connection getConnection(String server) throws RemoteException;
	
}
