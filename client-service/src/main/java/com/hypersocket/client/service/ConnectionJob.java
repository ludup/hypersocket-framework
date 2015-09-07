package com.hypersocket.client.service;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Locale;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.HypersocketVersion;
import com.hypersocket.Version;
import com.hypersocket.client.HypersocketClient;
import com.hypersocket.client.HypersocketClientAdapter;
import com.hypersocket.client.HypersocketClientListener;
import com.hypersocket.client.UserCancelledException;
import com.hypersocket.client.rmi.Connection;
import com.hypersocket.client.rmi.Connection.UpdateState;
import com.hypersocket.client.rmi.ResourceService;
import com.hypersocket.json.JsonResponse;
import com.hypersocket.netty.NettyClientTransport;

public class ConnectionJob extends TimerTask {

	static Logger log = LoggerFactory.getLogger(ConnectionJob.class);

	private String url;
	private Locale locale;
	private ClientServiceImpl clientService;
	private ResourceService resourceService;
	private ExecutorService worker;
	private ExecutorService boss;
	private Connection connection;
	private GUIRegistry guiRegistry;

	public ConnectionJob(String url, Locale locale,
			ClientServiceImpl clientService, ExecutorService boss,
			ExecutorService worker, ResourceService resourceService,
			Connection connection, GUIRegistry guiRegistry) {
		this.guiRegistry = guiRegistry;
		this.url = url;
		this.locale = locale;
		this.clientService = clientService;
		this.boss = boss;
		this.worker = worker;
		this.resourceService = resourceService;
		this.connection = connection;
	}

	@Override
	public void run() {

		if (log.isInfoEnabled()) {
			log.info("Connecting to " + url);
		}

		HypersocketClientListener<Connection> listener = new HypersocketClientAdapter<Connection>() {
			@Override
			public void disconnected(
					HypersocketClient<Connection> client,
					boolean onError) {
				clientService.disconnected(connection, client);
				guiRegistry
				.disconnected(
						connection,
						onError ? "Error occured during connection."
								: null);
				if (client.getAttachment().isStayConnected()
						&& onError) {
					try {
						clientService.scheduleConnect(connection);
					} catch (RemoteException e1) {
					}
				}
			}
		};

		ServiceClient client = null;
		try {

			client = new ServiceClient(new NettyClientTransport(boss, worker),
					locale, listener, resourceService, connection,
					guiRegistry);

			client.connect(connection.getHostname(), connection.getPort(),
					connection.getPath(), locale);

			if (log.isInfoEnabled()) {
				log.info("Connected to " + url);
			}
			guiRegistry.transportConnected(connection);

			log.info("Awaiting authentication for " + url);
			if (StringUtils.isBlank(connection.getUsername())
					|| StringUtils.isBlank(connection.getHashedPassword())) {
				client.login();

			} else {
				try {
					client.loginHttp(connection.getRealm(),
							connection.getUsername(),
							connection.getHashedPassword(), true);
				} catch (IOException ioe) {
					client.disconnect(true);
					client.connect(connection.getHostname(),
							connection.getPort(), connection.getPath(), locale);
					client.login();
				}
			}
			log.info("Received authentication for " + url);

			// Now get the current version and check against ours.
			String reply = client.getTransport().get("server/version");
			ObjectMapper mapper = new ObjectMapper();

			try {
				JsonResponse json = mapper.readValue(reply, JsonResponse.class);
				if (json.isSuccess()) {
					String[] versionAndSerial = json.getMessage().split(";");
					String version = versionAndSerial[0].trim();
					String serial = versionAndSerial[1].trim();

					/*
					 * Set the transient details. If an update is required it
					 * will be performed shortly by the client service (which
					 * will check all connections and update to the highest one
					 */
					connection.setServerVersion(version);
					connection.setSerial(serial);
					connection.setUpdateState(checkIfUpdateRequired(client,
							version) ? UpdateState.UPDATE_REQUIRED
							: UpdateState.UP_TO_DATE);
					client.addListener(listener);

					if (log.isInfoEnabled()) {
						log.info("Logged into " + url);
					}

					/* Tell the GUI we are now completely connected. The GUI
					 * should NOT yet load any resources, as we need to check if there are
					 * any updates to do first
					 */
					guiRegistry.ready(connection);
					
					/*
					 * Now check for updates. If there are any, we don't start any plugins
					 * for this connection, and the GUI will not be told to load its resources
					 */
					if(!clientService.update(connection, client)) {
						clientService.startPlugins(client);
						guiRegistry.loadResources(connection);
					}
					clientService.finishedConnecting(connection, client);
					
				} else {
					throw new Exception("Server refused to supply version. "
							+ json.getMessage());
				}
			} catch (Exception jpe) {
				if (log.isErrorEnabled()) {
					log.error("Failed to parse server version response "
							+ reply, jpe);
				}
				client.disconnect(false);
				guiRegistry.failedToConnect(connection, reply);
				clientService.failedToConnect(connection, jpe);
			}

		} catch (Throwable e) {
			if (log.isErrorEnabled()) {
				log.error("Failed to connect " + url, e);
			}
			guiRegistry.failedToConnect(connection, e.getMessage());
			clientService.failedToConnect(connection, e);

			if (!(e instanceof UserCancelledException)) {
				if (StringUtils.isNotBlank(connection.getUsername())
						&& StringUtils.isNotBlank(connection
								.getHashedPassword())) {
					if (connection.isStayConnected()) {
						try {
							clientService.scheduleConnect(connection);
							return;
						} catch (RemoteException e1) {
						}
					}
				}
			}
		} 

	}

	private boolean checkIfUpdateRequired(ServiceClient client,
			String versionString) {
		Version ourVersion = new Version(
				HypersocketVersion.getVersion("client-service"));

		// Compare
		Version version = new Version(versionString);
		if (version.compareTo(ourVersion) > 0) {
			log.info(String
					.format("Updating required, server is version %s, and we are version %s.",
							version.toString(), ourVersion.toString()));
			return true;
		} else if (version.compareTo(ourVersion) < 0) {
			log.warn(String
					.format("Client is on a later version than the server. This client is %s, where as the server is %s.",
							ourVersion.toString(), version.toString()));
		} else {
			log.info(String.format("Both server and client are on version %s",
					version.toString()));
		}
		return false;
	}

}
