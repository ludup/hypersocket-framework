package com.hypersocket.client.service;

import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.client.HypersocketClient;
import com.hypersocket.client.HypersocketClientListener;
import com.hypersocket.client.rmi.ClientService;
import com.hypersocket.client.rmi.ConfigurationService;
import com.hypersocket.client.rmi.Connection;
import com.hypersocket.client.rmi.ConnectionService;
import com.hypersocket.client.rmi.ConnectionStatus;
import com.hypersocket.client.rmi.ConnectionStatusImpl;
import com.hypersocket.client.rmi.GUICallback;
import com.hypersocket.client.rmi.ResourceService;

public class ClientServiceImpl implements ClientService,
		HypersocketClientListener<Connection> {

	static Logger log = LoggerFactory.getLogger(ClientServiceImpl.class);

	GUICallback gui;

	ConnectionService connectionService;
	ConfigurationService configurationService;
	ResourceService resourceService;

	ExecutorService bossExecutor;
	ExecutorService workerExecutor;
	Timer timer;

	Map<Connection, HypersocketClient<Connection>> activeClients = new HashMap<Connection, HypersocketClient<Connection>>();
	Map<Connection, HypersocketClient<Connection>> connectingClients = new HashMap<Connection, HypersocketClient<Connection>>();
	Map<Connection, Set<ServicePlugin>> connectionPlugins = new HashMap<Connection, Set<ServicePlugin>>();

	Semaphore startupLock = new Semaphore(1);

	public ClientServiceImpl(ConnectionService connectionService,
			ConfigurationService configurationService,
			ResourceService resourceService) {

		try {
			startupLock.acquire();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

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
			/* BPS - We need registration to wait until the client services
			 * are started up or there will be weird hibernate transaction
			 * errors if the GUI connects while the client is trying to 
			 * connect  
			 */
			startupLock.acquire();
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}

		try {
			this.gui = gui;
			gui.registered();
			if (log.isInfoEnabled()) {
				log.info("Registered GUI");
			}
		} finally {
			startupLock.release();
		}
	}

	@Override
	public void unregisterGUI(GUICallback gui) throws RemoteException {
		this.gui = null;
		gui.unregistered();
		if (log.isInfoEnabled()) {
			log.info("Unregistered GUI");
		}
	}

	public GUICallback getGUI() {
		return gui;
	}

	@Override
	public void ping() {

	}

	protected void notifyGui(String msg, int type) {
		try {
			if (gui != null) {
				gui.notify(msg, type);
			}
		} catch (Throwable e) {
			log.error("Failed to notify gui", e);
		}
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

		if (log.isInfoEnabled()) {
			log.info("Scheduling connect for connection id " + c.getId() + "/"
					+ c.getHostname());
		}

		timer.schedule(new ConnectionJob(createJobData(c)), 500);

	}

	@Override
	public void scheduleConnect(Connection c) throws RemoteException {

		if (log.isInfoEnabled()) {
			log.info("Scheduling connect for connection id " + c.getId() + "/"
					+ c.getHostname());
		}

		Integer reconnectSeconds = new Integer(configurationService.getValue(
				"client.reconnectInSeconds", "5"));

		timer.schedule(
				new ConnectionJob(createJobData(connectionService
						.getConnection(c.getId()))), reconnectSeconds * 1000);

	}

	Map<String, Object> createJobData(Connection c) throws RemoteException {

		Locale locale = new Locale(configurationService.getValue("ui.locale",
				"en"));
		Integer reconnectSeconds = new Integer(configurationService.getValue(
				"client.reconnectInSeconds", "30"));

		Map<String, Object> data = new HashMap<String, Object>();
		data.put("connection", c);
		data.put("service", this);
		data.put("bossExecutor", bossExecutor);
		data.put("workerExecutor", workerExecutor);
		data.put("resourceService", resourceService);
		data.put("gui", gui);
		data.put("locale", locale);
		data.put("reconnectSeconds", reconnectSeconds);
		data.put("url", getUrl(c));

		return data;
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
		return activeClients.containsKey(c) || connectingClients.containsKey(c);
	}

	@Override
	public void disconnect(Connection c) throws RemoteException {

		if (log.isInfoEnabled()) {
			log.info("Disconnecting connection with id " + c.getId() + "/"
					+ c.getHostname());
		}
		if (activeClients.containsKey(c)) {
			activeClients.get(c).disconnect(false);
		}

		/**
		 * Force removal here for final chance clean up
		 */
		activeClients.remove(c);
		connectingClients.remove(c);

		if (gui != null) {
			gui.disconnected(c, null);
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
		for (Connection c : connectionService.getConnections()) {
			ret.add(new ConnectionStatusImpl(c, getStatus(c)));
		}
		return ret;

	}

	@Override
	public void connected(HypersocketClient<Connection> client) {
		activeClients.put(client.getAttachment(), client);
		connectingClients.remove(client.getAttachment());
		startPlugins(client);

		notifyGui(client.getHost() + " connected", GUICallback.NOTIFY_CONNECT);
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
					plugin.start(client, resourceService, gui);

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
	public void disconnected(HypersocketClient<Connection> client,
			boolean onError) {
		activeClients.remove(client.getAttachment());
		connectingClients.remove(client.getAttachment());

		stopPlugins(client);

		notifyGui(client.getHost() + " disconnected",
				GUICallback.NOTIFY_DISCONNECT);
	}

	@Override
	public void connectStarted(HypersocketClient<Connection> client) {
		connectingClients.put(client.getAttachment(), client);
	}

	@Override
	public void connectFailed(HypersocketClient<Connection> client) {
		connectingClients.remove(client.getAttachment());
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
}
