package com.hypersocket.client.service;

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
import com.hypersocket.client.rmi.Connection;
import com.hypersocket.netty.NettyClientTransport;

public class ConnectionJob extends TimerTask {

	static Logger log = LoggerFactory.getLogger(ConnectionJob.class);

	Map<String,Object> data;
	
	public ConnectionJob(Map<String,Object> data) {
	}

	@Override
	public void run() {

		final Connection c = (Connection) data.get("connection");
		ExecutorService boss = (ExecutorService) data.get("bossExecutor");
		ExecutorService worker = (ExecutorService) data.get("workerExecutor");
		Locale locale = (Locale) data.get("locale");
		final ClientServiceImpl service = (ClientServiceImpl) data.get("service");
		
		String url = (String) data.get("url");

		if (log.isInfoEnabled()) {
			log.info("Connecting to " + url);
		}

		try {
			ServiceClient client = new ServiceClient(new NettyClientTransport(
					boss, worker), locale, service, c);

			client.connect(c.getHostname(), c.getPort(), c.getPath(), locale);

			if(log.isInfoEnabled()) {
				log.info("Connected to " + url);
			}
			
			if (StringUtils.isBlank(c.getUsername())
					|| StringUtils.isBlank(c.getHashedPassword())) {
				client.login();
				
			} else {
				client.loginHttp(c.getRealm(), c.getUsername(),
						c.getHashedPassword(), true);
			}
			
			client.addListener(new HypersocketClientAdapter<Connection>() {
				@Override
				public void disconnected(HypersocketClient<Connection> client, boolean onError) {
					if(client.getAttachment().isStayConnected() && onError) {
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

		} catch (Throwable e) {
			if (log.isErrorEnabled()) {
				log.error("Failed to connect " + url);
			}

			if (c.isStayConnected()) {
				try {
					service.scheduleConnect(c);
				} catch (RemoteException e1) {
				}
			}
		}

	}

}
