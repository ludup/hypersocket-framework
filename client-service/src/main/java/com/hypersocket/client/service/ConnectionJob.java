package com.hypersocket.client.service;

import java.rmi.RemoteException;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang.StringUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.client.HypersocketClient;
import com.hypersocket.client.HypersocketClientAdapter;
import com.hypersocket.client.rmi.Connection;
import com.hypersocket.netty.NettyClientTransport;

public class ConnectionJob implements Job {

	static Logger log = LoggerFactory.getLogger(ConnectionJob.class);

	public ConnectionJob() {
	}

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {

		JobDataMap data = context.getTrigger().getJobDataMap();

		Connection c = (Connection) data.get("connection");
		ExecutorService boss = (ExecutorService) data.get("bossExecutor");
		ExecutorService worker = (ExecutorService) data.get("workerExecutor");
		Locale locale = (Locale) data.get("locale");
		Integer reconnectSeconds = (Integer) data.get("reconnectSeconds");
		
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
				public void disconnected(HypersocketClient<Connection> client) {
					if(client.getAttachment().isStayConnected()) {
						try {
							service.connect(service.getConnectionService().getConnection(client.getAttachment().getId()));
						} catch (RemoteException e) {
							log.error("Error attempting to restart 'stay connected' client", e);
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
				if (log.isInfoEnabled()) {
					log.info("Scheduling reconnect attempt in "
							+ reconnectSeconds + " seconds to " + url);
				}
				try {

					Trigger trigger = TriggerBuilder
							.newTrigger()
							.withIdentity("connecting" + c.getId())
							.usingJobData(service.createJobData(c))
							.startAt(
									new Date(System.currentTimeMillis()
											+ (1000 * reconnectSeconds)))
							.build();

					context.getScheduler().rescheduleJob(
							context.getTrigger().getKey(), trigger);
				} catch (Exception e1) {
					log.error("Could not schedule job for url " + url, e1);
				}
			}
		}

	}

}
