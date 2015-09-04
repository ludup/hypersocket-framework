package com.hypersocket.client.service;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.Version;
import com.hypersocket.client.HypersocketClient;
import com.hypersocket.client.rmi.ClientService;
import com.hypersocket.client.rmi.ConfigurationService;
import com.hypersocket.client.rmi.Connection;
import com.hypersocket.client.rmi.Connection.UpdateState;
import com.hypersocket.client.rmi.ConnectionService;
import com.hypersocket.client.rmi.ConnectionStatus;
import com.hypersocket.client.rmi.ConnectionStatusImpl;
import com.hypersocket.client.rmi.GUICallback;
import com.hypersocket.client.rmi.ResourceService;
import com.hypersocket.client.service.updates.ClientUpdater;
import com.hypersocket.extensions.ExtensionPlace;

public class ClientServiceImpl implements ClientService {

	static Logger log = LoggerFactory.getLogger(ClientServiceImpl.class);

	ConnectionService connectionService;
	ConfigurationService configurationService;
	ResourceService resourceService;

	ExecutorService bossExecutor;
	ExecutorService workerExecutor;
	Timer timer;

	Map<Connection, HypersocketClient<Connection>> activeClients = new HashMap<Connection, HypersocketClient<Connection>>();
	Map<Connection, TimerTask> connectingClients = new HashMap<Connection, TimerTask>();
	Map<Connection, Set<ServicePlugin>> connectionPlugins = new HashMap<Connection, Set<ServicePlugin>>();

	Semaphore startupLock = new Semaphore(1);
	TimerTask updateTask;
	Runnable restartCallback;
	GUIRegistry guiRegistry;

	private boolean updating;

	private boolean guiNeedsSeparateUpdate;

	private int appsToUpdate;

	private ClientUpdater serviceUpdateJob;

	public ClientServiceImpl(ConnectionService connectionService,
			ConfigurationService configurationService,
			ResourceService resourceService, Runnable restartCallback,
			GUIRegistry guiRegistry) {

		try {
			startupLock.acquire();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		this.guiRegistry = guiRegistry;
		this.restartCallback = restartCallback;
		this.connectionService = connectionService;
		this.configurationService = configurationService;
		this.resourceService = resourceService;

		bossExecutor = Executors.newCachedThreadPool();
		workerExecutor = Executors.newCachedThreadPool();

		timer = new Timer(true);

	}

	@Override
	public void registerGUI(GUICallback gui) throws RemoteException {
		try {
			/*
			 * BPS - We need registration to wait until the client services are
			 * started up or there will be weird hibernate transaction errors if
			 * the GUI connects while the client is trying to connect
			 */
			startupLock.acquire();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		try {

			guiRegistry.registerGUI(gui);
			if (updating && guiNeedsSeparateUpdate) {
				/*
				 * If we register while an update is taking place, try to make
				 * the client catch up and show the update progress window
				 */
				guiRegistry.onUpdateInit(appsToUpdate);
				guiRegistry.onUpdateStart(ExtensionPlace.getDefault().getApp(),
						serviceUpdateJob.getTotalSize());
				guiRegistry.onUpdateProgress(ExtensionPlace.getDefault()
						.getApp(), 0, serviceUpdateJob.getTransfered(),serviceUpdateJob.getTotalSize());
				if (serviceUpdateJob.getTransfered() >= serviceUpdateJob
						.getTotalSize()) {
					guiRegistry.onUpdateComplete(ExtensionPlace.getDefault()
							.getApp(), serviceUpdateJob.getTransfered());
				}
			}
		} finally {
			startupLock.release();
		}
	}

	@Override
	public void unregisterGUI(GUICallback gui) throws RemoteException {
		guiRegistry.unregisterGUI(gui);
	}

	@Override
	public void ping() {

	}

	public boolean startService() {

		try {
			for (Connection c : connectionService.getConnections()) {
				if (c.isConnectAtStartup()) {
					connect(c);
				}
			}

			return true;
		} catch (RemoteException e) {
			log.error("Failed to start service", e);
			return false;
		} finally {
			startupLock.release();
		}
	}

	@Override
	public void connect(Connection c) throws RemoteException {
		checkValidConnect(c);
		if (log.isInfoEnabled()) {
			log.info("Scheduling connect for connection id " + c.getId() + "/"
					+ c.getHostname());
		}

		ConnectionJob task = createJob(c);
		connectingClients.put(c, task);
		timer.schedule(task, 500);
	}

	private void checkValidConnect(Connection c) throws RemoteException {
		if (connectingClients.containsKey(c)) {
			throw new RemoteException("Already connecting.");
		}
		if (activeClients.containsKey(c)) {
			throw new RemoteException("Already connected.");
		}
	}

	@Override
	public void scheduleConnect(Connection c) throws RemoteException {
		checkValidConnect(c);
		if (log.isInfoEnabled()) {
			log.info("Scheduling connect for connection id " + c.getId() + "/"
					+ c.getHostname());
		}

		Integer reconnectSeconds = new Integer(configurationService.getValue(
				"client.reconnectInSeconds", "5"));

		Connection connection = connectionService.getConnection(c.getId());
		if (connection == null) {
			log.warn("Ignoring a scheduled connection that no longer exists, probably deleted.");
		} else {
			timer.schedule(createJob(c), reconnectSeconds * 1000);
		}

	}

	protected ConnectionJob createJob(Connection c) throws RemoteException {
		return new ConnectionJob(getUrl(c), new Locale(
				configurationService.getValue("ui.locale", "en")), this,
				bossExecutor, workerExecutor, resourceService, c, guiRegistry);
	}

	protected String getUrl(Connection c) {
		return "https://" + c.getUsername() + "@" + c.getHostname()
				+ (c.getPort() != 443 ? ":" + c.getPort() : "") + c.getPath();
	}

	public void stopService() throws RemoteException {

		for (HypersocketClient<?> client : activeClients.values()) {
			client.disconnect(false);
		}

		activeClients.clear();
		connectingClients.clear();
		bossExecutor.shutdown();
		workerExecutor.shutdown();

		timer.cancel();

	}

	@Override
	public boolean isConnected(Connection c) throws RemoteException {
		// return activeClients.containsKey(c) ||
		// connectingClients.containsKey(c);
		return activeClients.containsKey(c);
	}

	@Override
	public void disconnect(Connection c) throws RemoteException {

		if (log.isInfoEnabled()) {
			log.info("Disconnecting connection with id " + c.getId() + "/"
					+ c.getHostname());
		}

		if (activeClients.containsKey(c)) {
			activeClients.remove(c).disconnect(false);
		} else if (connectingClients.containsKey(c)) {
			connectingClients.get(c).cancel();
			connectingClients.remove(c);

			/**
			 * Force removal here for final chance clean up
			 */
			guiRegistry.disconnected(c, null);
		} else {
			throw new RemoteException("Not connected.");
		}
	}

	@Override
	public int getStatus(Connection c) {
		return activeClients.containsKey(c) ? ConnectionStatus.CONNECTED
				: connectingClients.containsKey(c) ? ConnectionStatus.CONNECTING
						: ConnectionStatus.DISCONNECTED;
	}

	@Override
	public List<ConnectionStatus> getStatus() throws RemoteException {

		List<ConnectionStatus> ret = new ArrayList<ConnectionStatus>();
		Collection<Connection> connections = connectionService.getConnections();
		List<Connection> added = new ArrayList<Connection>();
		addConnections(ret, connections, added);
		addConnections(ret, activeClients.keySet(), added);
		addConnections(ret, connectingClients.keySet(), added);
		return ret;

	}

	@Override
	public boolean isGUINeedsUpdating() throws RemoteException {
		return guiNeedsSeparateUpdate;
	}

	public boolean isUpdating() {
		return updating;
	}

	public boolean update(final Connection c, ServiceClient client) throws RemoteException {
		Version highestVersionAvailable = null;
		if (c.getUpdateState() != UpdateState.UPDATE_REQUIRED || "true".equals(System
						.getProperty("hypersocket.development.noUpdates"))) {
			log.info("No updates to do.");
			guiNeedsSeparateUpdate = false;
		} else {
			log.info("Updating to " + highestVersionAvailable + " via "
					+ getUrl(c));

			try {
				updating = true;
				guiNeedsSeparateUpdate = true;

				/*
				 * For the client service, we use the local 'extension place'
				 */
				appsToUpdate = 1;
				serviceUpdateJob = new ClientUpdater(guiRegistry, c,
						client, ExtensionPlace.getDefault());

				/*
				 * For the GUI, we get the extension place remotely, as the GUI
				 * itself is best placed to know what extensions it has and
				 * where they stored.
				 * 
				 * However, it's possible the GUI is not yet running, so we only
				 * do this if it is available. If this happens we may need to
				 * update the GUI as well when it eventually
				 */
				ClientUpdater guiJob = null;
				if (guiRegistry.hasGUI()) {
					appsToUpdate++;
					guiNeedsSeparateUpdate = false;
					guiJob = new ClientUpdater(guiRegistry, c,
							client, guiRegistry.getGUI()
									.getExtensionPlace());
				}

				try {
					guiRegistry.onUpdateInit(appsToUpdate);

					int updates = 0;

					if (serviceUpdateJob.update()) {
						updates++;
					}

					if (guiJob != null && guiJob.update()) {
						updates++;
					}

					if (updates > 0) {

						/*
						 * If when we started the update, the GUI wasn't
						 * attached, but it is now, then instead of restarting
						 * immediately, try to update any client extensions too
						 */
						if (guiNeedsSeparateUpdate && guiRegistry.hasGUI()) {
							guiNeedsSeparateUpdate = false;
							appsToUpdate = 1;
							guiJob = new ClientUpdater(guiRegistry, c,
									client, guiRegistry.getGUI()
											.getExtensionPlace());
							guiRegistry.onUpdateInit(appsToUpdate);
							guiJob.update();

							guiRegistry.onUpdateDone(null);
							log.info("Update complete, restarting.");
							restartCallback.run();
						} else {
							guiRegistry.onUpdateDone(null);
							log.info("Update complete, restarting.");
							restartCallback.run();
						}
					} else {
						guiRegistry.onUpdateDone("Nothing to update.");
					}

				} catch (IOException e) {
					log.error("Failed to execute update job.", e);
				}

				return true;
			} catch (RemoteException re) {
				log.error(
						"Failed to get GUI extension information. Update aborted.",
						re);
			} finally {
				updating = false;
			}
		}
		return false;
	}

	private void addConnections(List<ConnectionStatus> ret,
			Collection<Connection> connections, List<Connection> added) {
		for (Connection c : connections) {
			if (!added.contains(c)) {
				ret.add(new ConnectionStatusImpl(c, getStatus(c)));
				added.add(c);
			}
		}
	}

	protected void stopPlugins(HypersocketClient<Connection> client) {

		Set<ServicePlugin> plugins = connectionPlugins.get(client
				.getAttachment());
		for (ServicePlugin plugin : plugins) {
			try {
				plugin.stop();
			} catch (Throwable e) {
				log.error("Failed to stop plugin " + plugin.getName(), e);
			}
		}
	}

	protected void startPlugins(HypersocketClient<Connection> client) {
		Enumeration<URL> urls;

		if (log.isInfoEnabled()) {
			log.info("Starting plugins");
		}
		if (!connectionPlugins.containsKey(client.getAttachment())) {
			connectionPlugins.put(client.getAttachment(),
					new HashSet<ServicePlugin>());
		}
		try {
			urls = getClass().getClassLoader().getResources(
					"service-plugin.properties");

			if (log.isInfoEnabled() && !urls.hasMoreElements()) {
				log.info("There are no plugins in classpath");

				urls = getClass().getClassLoader().getResources(
						"/service-plugin.properties");
			}

			while (urls.hasMoreElements()) {

				URL url = urls.nextElement();

				if (log.isInfoEnabled()) {
					log.info("Found plugin at " + url.toExternalForm());
				}
				try {

					Properties p = new Properties();
					p.load(url.openStream());

					String name = p.getProperty("plugin.name");
					String clz = p.getProperty("plugin.class");

					if (log.isInfoEnabled()) {
						log.info("Starting plugin " + name + "[" + clz + "]");
					}

					@SuppressWarnings({ "unchecked" })
					Class<ServicePlugin> pluginClz = (Class<ServicePlugin>) Class
							.forName(clz);

					ServicePlugin plugin = pluginClz.newInstance();
					plugin.start(client, resourceService, guiRegistry);

					connectionPlugins.get(client.getAttachment()).add(plugin);
				} catch (Throwable e) {
					log.error("Failed to start plugin", e);
				}
			}

		} catch (Throwable e) {
			log.error("Failed to start plugins", e);
		}
	}

	@Override
	public ConnectionService getConnectionService() throws RemoteException {
		return connectionService;
	}

	@Override
	public ConfigurationService getConfigurationService()
			throws RemoteException {
		return configurationService;
	}

	@Override
	public byte[] getBlob(Connection connection, String path, long timeout)
			throws IOException {

		HypersocketClient<Connection> s = null;
		for (HypersocketClient<Connection> a : activeClients.values()) {
			if (a.getAttachment() == connection) {
				s = a;
				break;
			}
		}
		if (s == null) {
			throw new RemoteException("No connection for " + connection);
		}
		try {
			return s.getTransport().getBlob(path, timeout);
		} catch (IOException e) {
			throw new RemoteException(e.getMessage());
		}

	}

	@Override
	public byte[] getBlob(String host, String path, long timeout)
			throws RemoteException {
		HypersocketClient<Connection> s = null;
		for (HypersocketClient<Connection> a : activeClients.values()) {
			if (a.getHost().equals(host)) {
				s = a;
				break;
			}
		}
		if (s == null) {
			throw new RemoteException("No connection for " + host);
		}
		try {
			return s.getTransport().getBlob(path, timeout);
		} catch (IOException e) {
			throw new RemoteException(e.getMessage());
		}
	}

	@Override
	public Connection save(Connection c) throws RemoteException {
		// If a non-persistent connection is now being saved as a persistent
		// one, then update our maps
		Connection newConnection = connectionService.save(c);

		if (c.getId() == null && newConnection.getId() != null) {
			log.info(String.format(
					"Saving non-persistent connection, now has ID %d",
					newConnection.getId()));
		}

		if (activeClients.containsKey(c)) {
			activeClients.put(newConnection, activeClients.remove(c));
		}
		if (connectingClients.containsKey(c)) {
			connectingClients.put(newConnection, connectingClients.remove(c));
		}
		if (connectionPlugins.containsKey(c)) {
			connectionPlugins.put(newConnection, connectionPlugins.remove(c));
		}
		return newConnection;
	}

	public void finishedConnecting(Connection connection,
			HypersocketClient<Connection> client) {
		connectingClients.remove(connection);
		activeClients.put(connection, client);
		guiRegistry.started(connection);
	}

	public void failedToConnect(Connection connection, Throwable jpe) {
		connectingClients.remove(connection);
	}

	public void disconnected(Connection connection,
			HypersocketClient<Connection> client) {
		activeClients.remove(client.getAttachment());
		connectingClients.remove(client.getAttachment());
		stopPlugins(client);
		guiRegistry.notify(client.getHost() + " disconnected",
				GUICallback.NOTIFY_DISCONNECT);
	}
}
