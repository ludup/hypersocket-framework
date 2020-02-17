package com.hypersocket.tasks.ip.unblock;

import java.net.UnknownHostException;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.ip.DefaultIPRestrictionProvider;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRepository;
import com.hypersocket.scheduler.PermissionsAwareJob;
import com.hypersocket.tasks.ip.block.BlockIPTask;

public class UnblockIPJob extends PermissionsAwareJob {

	static Logger log = LoggerFactory.getLogger(UnblockIPJob.class);
	
	@Autowired
	private DefaultIPRestrictionProvider ipRestrictionProvider;
	
	@Autowired
	private RealmRepository realmRepository; 
	
	@Autowired
	private BlockIPTask blockIPTask;
	
	public UnblockIPJob() {
	}

	@Override
	protected void executeJob(JobExecutionContext context)
			throws JobExecutionException {
	
		String addr = (String) context.getTrigger().getJobDataMap().get("addr");
		
		if(addr==null) {
			throw new JobExecutionException("UblockIP job requires InetAddress parameter addr!");
		}
		
		try {
			if(log.isInfoEnabled()) {
				log.info("Unblocking IP address " + addr.toString());
			}
			
			ipRestrictionProvider.unblockIPAddress(addr);
			
			Realm currentRealm = realmRepository.getRealmById(context.getTrigger().getJobDataMap().getLong("realm"));
			
			blockIPTask.notifyUnblock(currentRealm.getName() + "_" + addr, true);
			
			if(log.isInfoEnabled()) {
				log.info("Unblocked IP address " + addr.toString());
			}
		} catch (UnknownHostException e) {
			log.error("Failed to unblock IP address " + addr.toString());
		}

	}

}
