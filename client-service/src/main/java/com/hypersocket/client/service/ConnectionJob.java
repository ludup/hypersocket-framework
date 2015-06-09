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

import com.hypersocket.client.HypersocketClient;
import com.hypersocket.client.HypersocketClientAdapter;
import com.hypersocket.client.UserCancelledException;
import com.hypersocket.client.rmi.Connection;
import com.hypersocket.client.rmi.GUICallback;
import com.hypersocket.client.rmi.ResourceService;
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
					client.login();					
				}
			}
			log.info("Received authentication for " + url);

			// Now get the current version and check against ours.
			String response[] = client.getTransport().get("server/version")
					.split(";");
			if (response.length != 2) {
				if (log.isErrorEnabled()) {
					log.error("Failed to get version from server "
							+ response.length);
				}
				client.disconnect(false);
				if (callback != null) {
					callback.failedToConnect(c,
							"Failed to get version from server "
									+ response.length);
				}
			} else {
				// String version = response[0];
				// String serial = response[1];

				client.addListener(new HypersocketClientAdapter<Connection>() {
					@Override
					public void connectFailed(Exception e,
							HypersocketClient<Connection> client) {
						super.connectFailed(e, client);
						try {
							callback.failedToConnect(client.getAttachment(), e.getMessage());
						} catch (RemoteException e1) {
						}
					}

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

}
