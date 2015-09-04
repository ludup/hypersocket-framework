package com.hypersocket.client.rmi;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import com.hypersocket.client.Prompt;
import com.hypersocket.extensions.ExtensionDefinition;
import com.hypersocket.extensions.ExtensionPlace;

public interface GUICallback extends Serializable, Remote {

	public static final int NOTIFY_ERROR = 0;
	public static final int NOTIFY_WARNING = 1;
	public static final int NOTIFY_INFO = 2;
	public static final int NOTIFY_CONNECT = 3;
	public static final int NOTIFY_DISCONNECT = 4;

	void registered() throws RemoteException;

	void unregistered() throws RemoteException;

	void notify(String msg, int type) throws RemoteException;

	Map<String, String> showPrompts(List<Prompt> prompts, int attempts, boolean success)
			throws RemoteException;

	int executeAsUser(ApplicationLauncherTemplate launcherTemplate,
			String clientUsername, String connectedHostname)
			throws RemoteException;

	void disconnected(Connection connection, String errorMessage)
			throws RemoteException;

	void failedToConnect(Connection connection, String errorMessage)
			throws RemoteException;

	void transportConnected(Connection connection) throws RemoteException;

	void ready(Connection connection) throws RemoteException;

	void started(Connection connection) throws RemoteException;

	void loadResources(Connection connection) throws RemoteException;
	
	void onUpdateInit(int expectedApps) throws RemoteException;
	
	void onUpdateStart(String app, long totalBytesExpected) throws RemoteException;
	
	void onUpdateProgress(String app, long sincelastProgress, long totalSoFar, long totalBytesExpected) throws RemoteException;
	
	void onUpdateComplete(long totalBytesTransfered, String app) throws RemoteException;
	
	void onUpdateFailure(String app, String message) throws RemoteException;
	
	void onExtensionUpdateComplete(String app, ExtensionDefinition def) throws RemoteException;
	
	void onUpdateDone(String failureMessage)  throws RemoteException;
	
	ExtensionPlace getExtensionPlace() throws RemoteException;

}
