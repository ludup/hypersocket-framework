package com.hypersocket.scheduler;

import java.util.Properties;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.upgrade.UpgradeService;
import com.hypersocket.upgrade.UpgradeServiceListener;
import com.hypersocket.util.DatabaseInformation;

@Service
public class LocalSchedulerServiceImpl extends AbstractSchedulerServiceImpl implements LocalSchedulerService {

	@Autowired 
	AutowiringSpringBeanJobFactory autowiringSpringBeanJobFactory;
	
	@Autowired
	DatabaseInformation databaseInformation;
	
	@Autowired
	UpgradeService upgradeService; 
	
	protected Scheduler configureScheduler() throws SchedulerException {

		Properties props = new Properties();
		props.setProperty("org.quartz.scheduler.instanceName", "LocalScheduler");
		props.setProperty("org.quartz.scheduler.instanceId", "LOCAL_JOBS");
		props.setProperty("org.quartz.threadPool.class", "com.hypersocket.scheduler.LocalThreadPool");
		props.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
		props.setProperty("org.quartz.threadPool.threadCount", "5");
		
		StdSchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory(props);
		
		final Scheduler scheduler = schedFact.getScheduler();
		scheduler.setJobFactory(autowiringSpringBeanJobFactory);
		
		upgradeService.registerListener(new UpgradeServiceListener() {
			
			@Override
			public void onUpgradeComplete() {
				try {
					scheduler.start();
				} catch (SchedulerException e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
			}
		});
		return scheduler;
	}
}
