package com.hypersocket.client.service;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Locale;
import java.util.Map;
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
import com.hypersocket.client.UserCancelledException;
import com.hypersocket.client.rmi.Connection;
import com.hypersocket.client.rmi.Connection.UpdateState;
import com.hypersocket.client.rmi.GUICallback;
import com.hypersocket.client.rmi.ResourceService;
import com.hypersocket.json.JsonResponse;
import com.hypersocket.netty.NettyClientTransport;

public class ConnectionJob extends TimerTask {

	static Logger log = LoggerFactory.getLogger(ConnectionJob.class);

	Map<String, Object> data;

	public ConnectionJob(Map<String, Object> data) {
		this.data = data;
	}

	@Override
	public void run() {

		final Connection c = (Connection) data.get("connection");
		ExecutorService boss = (ExecutorService) data.get("bossExecutor");
		ExecutorService worker = (ExecutorService) data.get("workerExecutor");
		final GUICallback callback = (GUICallback) data.get("gui");

		Locale locale = (Locale) data.get("locale");
		final ClientServiceImpl service = (ClientServiceImpl) data
				.get("service");
		ResourceService resourceService = (ResourceService) data
				.get("resourceService");

		String url = (String) data.get("url");

		if (log.isInfoEnabled()) {
			log.info("Connecting to " + url);
		}

		ServiceClient client = null;
		try {

			client = new ServiceClient(new NettyClientTransport(
					boss, worker), locale, service, resourceService, c);

			client.connect(c.getHostname(), c.getPort(), c.getPath(), locale);

			if (log.isInfoEnabled()) {
				log.info("Connected to " + url);
			}
			if (callback != null) {
				callback.transportConnected(c);
			}

			log.info("Awaiting authentication for " + url);
			if (StringUtils.isBlank(c.getUsername())
					|| StringUtils.isBlank(c.getHashedPassword())) {
				client.login();

			} else {
				try {
					client.loginHttp(c.getRealm(), c.getUsername(),
						c.getHashedPassword(), true);
				}
				catch(IOException ioe) {
					client.disconnect(true);
					client.connect(c.getHostname(), c.getPort(), c.getPath(), locale);
					client.login();					
				}
			}
			log.info("Received authentication for " + url);

			// Now get the current version and check against ours.
			String reply = client.getTransport().get("server/version");
			ObjectMapper mapper = new ObjectMapper();
			
			try {
				JsonResponse json = mapper.readValue(reply, JsonResponse.class);
				if(json.isSuccess()) {
					String[] versionAndSerial = json.getMessage().split(";");
					String version = versionAndSerial[0].trim();
					String serial = versionAndSerial[1].trim();

					/* Set the transient details. If an update is required it will be performed shortly
					 * by the client service (which will check all connections and update to the highest
					 * one 
					 */
					c.setServerVersion(version);
					c.setSerial(serial);
					c.setUpdateState(checkIfUpdateRequired(client, version) ? UpdateState.UPDATE_REQUIRED : UpdateState.UP_TO_DATE);
	
					client.addListener(new HypersocketClientAdapter<Connection>() {
						@Override
						public void disconnected(
								HypersocketClient<Connection> client,
								boolean onError) {
							try {
								callback.disconnected(
										c,
										onError ? "Error occured during connection."
												: null);
							} catch (RemoteException e) {
							}
							if (client.getAttachment().isStayConnected() && onError) {
								try {
									service.scheduleConnect(c);
								} catch (RemoteException e1) {
								}
							}
						}
					});
	
					if (log.isInfoEnabled()) {
						log.info("Logged into " + url);
					}
	
					if (callback != null) {
						callback.ready(c);
					}
					
					// Trigger interest in possibly updating
					service.maybeUpdate(c);
				}
				else {
					throw new Exception("Server refused to supply version. " + json.getMessage());
				}
			}
			catch(Exception jpe) {
				if (log.isErrorEnabled()) {
					log.error("Failed to parse server version response "
							+ reply, jpe);
				}
				client.disconnect(false);
				if (callback != null) {
					callback.failedToConnect(c,
							"Failed to get version from server "
									+ reply);
				}
			}
			
		} catch (Throwable e) {
			if (log.isErrorEnabled()) {
				log.error("Failed to connect " + url, e);
			}
			if (callback != null) {
				try {
					callback.failedToConnect(c, e.getMessage());
				} catch (RemoteException e2) {
					//
				}
			}
			
			if (!(e instanceof UserCancelledException)) {
				if (StringUtils.isNotBlank(c.getUsername())
						&& StringUtils.isNotBlank(c.getHashedPassword())) {
					if (c.isStayConnected()) {
						try {
							service.scheduleConnect(c);
						} catch (RemoteException e1) {
						}
					}
				}
			}
		}

	}
	
	private boolean checkIfUpdateRequired(ServiceClient client, String versionString) {
		Version ourVersion = new Version(HypersocketVersion.getVersion("client-service"));
		
		// Compare
		Version version = new Version(versionString);
		if(version.compareTo(ourVersion) > 0) {
			log.info(String.format("Updating required, server is version %s, and we are version %s.", version.toString(), ourVersion.toString()));
			return true;
		}
		else if(version.compareTo(ourVersion) < 0) {
			log.warn(String.format("Client is on a later version than the server. This client is %s, where as the server is %s.", ourVersion.toString(), version.toString()));
		}
		else {
			log.info(String.format("Both server and client are on version %s", version.toString()));
		}
		return false;
	}

}
