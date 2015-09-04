package com.hypersocket.client.gui.jfx;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javafx.application.Platform;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.client.Prompt;
import com.hypersocket.client.rmi.ApplicationLauncherTemplate;
import com.hypersocket.client.rmi.ClientService;
import com.hypersocket.client.rmi.ConfigurationService;
import com.hypersocket.client.rmi.Connection;
import com.hypersocket.client.rmi.ConnectionService;
import com.hypersocket.client.rmi.ConnectionStatus;
import com.hypersocket.client.rmi.GUICallback;
import com.hypersocket.client.rmi.ResourceService;
import com.hypersocket.extensions.ExtensionDefinition;
import com.hypersocket.extensions.ExtensionPlace;

@SuppressWarnings({ "serial" })
public class Bridge extends UnicastRemoteObject implements GUICallback {

	static Logger log = LoggerFactory.getLogger(Main.class);

	private ConnectionService connectionService;
	private ResourceService resourceService;
	private ClientService clientService;
	private ConfigurationService configurationService;
	private boolean connected;
	private List<Listener> listeners = new ArrayList<>();

	static int failedConnectionAttempts = 0;
	
	public interface Listener {

		void loadResources(Connection connection);

		void connecting(Connection connection);

		void finishedConnecting(Connection connection, Exception e);

		void started(Connection connection);

		void disconnecting(Connection connection);

		void disconnected(Connection connection, Exception e);

		void bridgeEstablished();

		void bridgeLost();

		void ping();

		Map<String, String> showPrompts(List<Prompt> prompts, int attempts, boolean success);

		void initUpdate(int apps);

		void initDone(String errorMessage);

		void startingUpdate(String app, long totalBytesExpected);

		void updateProgressed(String app, long sincelastProgress,
				long totalSoFar, long totalBytesExpected);

		void updateComplete(String app, long totalBytesTransfered);

		void updateFailure(String app, String message);

		void extensionUpdateComplete(String app, ExtensionDefinition def);
	}

	public Bridge() throws RemoteException {
		super();
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					if (clientService != null) {
						clientService.unregisterGUI(Bridge.this);
					}
				} catch (RemoteException e) {
				}
			}
		});
		new RMIConnectThread().start();
	}

	public void addListener(Listener l) {
		listeners.add(l);
	}

	public void removeListener(Listener l) {
		listeners.add(l);
	}

	public ConnectionService getConnectionService() {
		return connectionService;
	}

	public ResourceService getResourceService() {
		return resourceService;
	}

	public ClientService getClientService() {
		return clientService;
	}

	public ConfigurationService getConfigurationService() {
		return configurationService;
	}

	public boolean isConnected() {
		return connected;
	}

	private void connectToService() throws RemoteException, NotBoundException {

		Properties properties = new Properties();
		FileInputStream in;
		try {
			if (Boolean.getBoolean("hypersocket.development")) {
				in = new FileInputStream(System.getProperty("user.home")
						+ File.separator + ".hypersocket" + File.separator
						+ "conf" + File.separator + "rmi.properties");
			} else {
				in = new FileInputStream("conf" + File.separator
						+ "rmi.properties");
			}

			try {
				properties.load(in);
			} finally {
				in.close();
			}
		} catch (IOException e2) {
			e2.printStackTrace();
		}
		int port = Integer.parseInt(properties.getProperty("port", "50000"));

		try {

			if (log.isDebugEnabled()) {
				log.debug("Connecting to local service on port " + port);
			}
			Registry registry = LocateRegistry.getRegistry(port);

			connectionService = (ConnectionService) registry
					.lookup("connectionService");

			configurationService = (ConfigurationService) registry
					.lookup("configurationService");

			resourceService = (ResourceService) registry
					.lookup("resourceService");

			clientService = (ClientService) registry.lookup("clientService");

			clientService.registerGUI(this);
			failedConnectionAttempts = 0;
			connected = true;
			for (Listener l : listeners) {
				l.bridgeEstablished();
			}

			new RMIStatusThread().start();
		} catch (Throwable e) {
			int maxAttempts = Integer.parseInt(System.getProperty("hypersocket.maxAttempts", "0"));
			if(maxAttempts > 0 && failedConnectionAttempts > maxAttempts) {
				log.info("Shutting down client. Cannot connect to service");
				System.exit(0);
			}
			failedConnectionAttempts++;
			if (log.isDebugEnabled()) {
				log.debug("Failed to connect to local service on port " + port);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				if(log.isInfoEnabled()) {
					log.info("Interrupted during sleep waiting for service. Exiting");
				}
				System.exit(0);
			}
			new RMIConnectThread().start();
		}

	}

	class RMIConnectThread extends Thread {
		public void run() {
			try {
				connectToService();
			} catch (Exception e) {
				if (log.isDebugEnabled()) {
					log.debug("Failed to connect to service", e);
				}
			}
		}
	}

	class RMIStatusThread extends Thread {
		public void run() {
			try {
				boolean running = true;
				while (running) {
					try {
						Thread.sleep(1000);
					} catch (Exception e) {
					}
					if (clientService != null) {
						try {
							clientService.ping();
							for (Listener l : listeners) {
								l.ping();
							}
						} catch (Exception e) {
							running = false;
							log.error("Failed to get local service status", e);
						}
					}
				}
			} finally {
				connected = false;
				for (Listener l : listeners) {
					l.bridgeLost();
				}
				new RMIConnectThread().start();
			}
		}
	}

	@Override
	public void registered() throws RemoteException {
		System.err.println("[[REGISTERED]]");
	}

	@Override
	public void unregistered() throws RemoteException {
		System.err.println("[[UNREGISTERED]]");
	}

	@Override
	public void notify(String msg, int type) throws RemoteException {
		System.err.println("[[NOTIFY]] " + msg + " (" + type + ")");
		Platform.runLater(new Runnable() {
			public void run() {
				Dock.getInstance().notify(msg, type);
				// Window parent = Dock.getInstance().getStage();
				//
				// switch (type) {
				// case NOTIFY_CONNECT:
				// Notifier.INSTANCE.notifySuccess(parent,
				// I18N.getResource("notify.connect"), msg);
				// break;
				// case NOTIFY_DISCONNECT:
				// Notifier.INSTANCE.notifySuccess(parent,
				// I18N.getResource("notify.disconnect"), msg);
				// break;
				// case NOTIFY_INFO:
				// Notifier.INSTANCE.notifyInfo(parent,
				// I18N.getResource("notify.information"), msg);
				// break;
				// case NOTIFY_WARNING:
				// Notifier.INSTANCE.notifyWarning(parent,
				// I18N.getResource("notify.warning"), msg);
				// break;
				// case NOTIFY_ERROR:
				// Notifier.INSTANCE.notifyError(parent,
				// I18N.getResource("notify.error"), msg);
				// break;
				// }
			}
		});
	}

	@Override
	public Map<String, String> showPrompts(List<Prompt> prompts, int attempts, boolean success)
			throws RemoteException {
		for (Listener l : new ArrayList<Listener>(listeners)) {
			Map<String, String> m = l.showPrompts(prompts, attempts, success);
			if (m != null) {
				return m;
			}
		}
		return null;
	}

	@Override
	public int executeAsUser(ApplicationLauncherTemplate launcherTemplate,
			String clientUsername, String connectedHostname)
			throws RemoteException {
		return 0;
	}

	public void disconnect(Connection connection) throws RemoteException {
		for (Listener l : new ArrayList<Listener>(listeners)) {
			l.disconnecting(connection);
		}
		log.info(String.format("Disconnecting from https://%s:%d/%s",
				connection.getHostname(), connection.getPort(),
				connection.getPath()));
		clientService.disconnect(connection);
	}

	public void connect(Connection connection) throws RemoteException {
		for (Listener l : new ArrayList<Listener>(listeners)) {
			l.connecting(connection);
		}
		log.info(String.format("Connecting to https://%s:%d/%s",
				connection.getHostname(), connection.getPort(),
				connection.getPath()));
		clientService.connect(connection);
	}

	@Override
	public void disconnected(Connection connection, String errorMessage)
			throws RemoteException {
		log.info("Bridge disconnected " + connection  + " (" + errorMessage + ")");
		Exception e = errorMessage == null ? null : new Exception(errorMessage);
		for (Listener l : new ArrayList<Listener>(listeners)) {
			l.disconnected(connection, e);
		}
	}

	@Override
	public void transportConnected(Connection connection)
			throws RemoteException {
	}

	@Override
	public void started(Connection connection) throws RemoteException {
		log.info("Connection " + connection + " is now started");
		for (Listener l : new ArrayList<Listener>(listeners)) {
			l.started(connection);
		}
		notify(connection.getHostname() + " connected",
				GUICallback.NOTIFY_CONNECT);
	}

	@Override
	public void ready(Connection connection) throws RemoteException {
		log.info("Connection " + connection + " is now ready");
		for (Listener l : new ArrayList<Listener>(listeners)) {
			l.finishedConnecting(connection, null);
		}
	}

	@Override
	public void loadResources(Connection connection) throws RemoteException {
		log.info("Connection " + connection + " should load resources");
		for (Listener l : new ArrayList<Listener>(listeners)) {
			l.loadResources(connection);
		}
	}

	@Override
	public void failedToConnect(Connection connection, String errorMessage)
			throws RemoteException {
		log.error(String.format("Failed to connect. %s", errorMessage));
		Exception e = errorMessage == null ? null : new Exception(errorMessage);
		for (Listener l : new ArrayList<Listener>(listeners)) {
			l.finishedConnecting(connection, e);
		}
	}

	public int getActiveConnections() {
		int active = 0;
		if (isConnected()) {
			try {
				for (ConnectionStatus s : clientService.getStatus()) {
					if (s.getStatus() == ConnectionStatus.CONNECTED) {
						active++;
					}
				}
			} catch (RemoteException e) {
				log.error("Failed to get active connections.", e);
			}
		}
		return active;
	}

	public void disconnectAll() {
		try {
			for (ConnectionStatus s : clientService.getStatus()) {
				if (s.getStatus() == ConnectionStatus.CONNECTED
						|| s.getStatus() == ConnectionStatus.CONNECTING) {
					try {
						disconnect(s.getConnection());
					} catch (RemoteException re) {
						log.error("Failed to disconnect "
								+ s.getConnection().getId(), re);
					}

				}
			}
		} catch (RemoteException e) {
			log.error("Failed to disconnect all.", e);
		}
	}

	@Override
	public void onUpdateStart(String app, long totalBytesExpected)
			throws RemoteException {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				for (Listener l : new ArrayList<Listener>(listeners)) {
					l.startingUpdate(app, totalBytesExpected);
				}
			}
		});
	}

	@Override
	public void onUpdateProgress(String app, long sincelastProgress,
			long totalSoFar, long totalBytesExpected) throws RemoteException {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				for (Listener l : new ArrayList<Listener>(listeners)) {
					l.updateProgressed(app, sincelastProgress, totalSoFar, totalBytesExpected);
				}
			}
		});
	}

	@Override
	public void onUpdateComplete(long totalBytesTransfered, String app)
			throws RemoteException {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				for (Listener l : new ArrayList<Listener>(listeners)) {
					l.updateComplete(app, totalBytesTransfered);
				}
			}
		});
	}

	@Override
	public void onUpdateFailure(String app, String message)
			throws RemoteException {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				for (Listener l : new ArrayList<Listener>(listeners)) {
					l.updateFailure(app, message);
				}
			}
		});
	}

	@Override
	public void onExtensionUpdateComplete(String app, ExtensionDefinition def)
			throws RemoteException {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				for (Listener l : new ArrayList<Listener>(listeners)) {
					l.extensionUpdateComplete(app, def);
				}
			}
		});
	}

	@Override
	public void onUpdateInit(final int expectedApps) throws RemoteException {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				for (Listener l : new ArrayList<Listener>(listeners)) {
					l.initUpdate(expectedApps);
				}
			}
		});
	}

	@Override
	public ExtensionPlace getExtensionPlace() {
		return ExtensionPlace.getDefault();
	}

	@Override
	public void onUpdateDone(final String failureMessage)
			throws RemoteException {
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				for (Listener l : new ArrayList<Listener>(listeners)) {
					l.initDone(failureMessage);
				}
			}
		});

	}

	public boolean isServiceUpdating() {
		try {
			return clientService != null && clientService.isUpdating();
		}	
		catch(RemoteException re) {
			return false;
		}
	}
}
