package com.hypersocket.triggers.actions.ip;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.scheduler.PermissionsAwareJob;
import com.hypersocket.server.HypersocketServer;

public class UnblockIPJob extends PermissionsAwareJob {

	static Logger log = LoggerFactory.getLogger(UnblockIPJob.class);
	
	@Autowired
	HypersocketServer server;
	
	public UnblockIPJob() {
	}

	@Override
	protected void executeJob(JobExecutionContext context)
			throws JobExecutionException {
	
		InetAddress addr = (InetAddress) context.getTrigger().getJobDataMap().get("addr");
		
		if(addr==null) {
			throw new JobExecutionException("UblockIP job requires InetAddress parameter addr!");
		}
		
		try {
			if(log.isInfoEnabled()) {
				log.info("Unblocking IP address " + addr.toString());
			}
			
			server.unblockAddress(addr);
			
			if(log.isInfoEnabled()) {
				log.info("Unblocked IP address " + addr.toString());
			}
		} catch (UnknownHostException e) {
			log.error("Failed to unblock IP address " + addr.toString());
		}

	}

}
