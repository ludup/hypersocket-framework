package com.hypersocket.client.rmi;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Map;

import com.hypersocket.client.Prompt;

public interface GUICallback extends Serializable, Remote {

	public static final int NOTIFY_ERROR = 0;
	public static final int NOTIFY_WARNING = 1;
	public static final int NOTIFY_INFO = 2;
	public static final int NOTIFY_CONNECT = 3;
	public static final int NOTIFY_DISCONNECT = 4;

	void registered() throws RemoteException;

	void unregistered() throws RemoteException;

	void notify(String msg, int type) throws RemoteException;

	Map<String, String> showPrompts(List<Prompt> prompts)
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

}
