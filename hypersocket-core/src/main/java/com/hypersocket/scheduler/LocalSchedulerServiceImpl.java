package com.hypersocket.scheduler;

import java.util.Properties;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.scheduler.listener.AppJobTriggerListener;
import com.hypersocket.upgrade.UpgradeService;
import com.hypersocket.upgrade.UpgradeServiceListener;

@Service
public class LocalSchedulerServiceImpl extends AbstractSchedulerServiceImpl implements LocalSchedulerService {

	@Autowired 
	private AutowiringSpringBeanJobFactory autowiringSpringBeanJobFactory;
	
	@Autowired
	private UpgradeService upgradeService; 
	
	protected Scheduler configureScheduler() throws SchedulerException {

		Properties props = new Properties();
		props.setProperty("org.quartz.scheduler.instanceName", "LocalScheduler");
		props.setProperty("org.quartz.scheduler.instanceId", "LOCAL_JOBS");
		props.setProperty("org.quartz.threadPool.class", "com.hypersocket.scheduler.LocalThreadPool");
		props.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
		/* We don't have many local jobs, a low count is fine. min(8,Cores) for production, or just a fixed 2 for development  */
		if(Boolean.getBoolean("hypersocket.development")) {
			props.setProperty("org.quartz.threadPool.threadCount", "2");
		}
		else {
			props.setProperty("org.quartz.threadPool.threadCount", String.valueOf(Math.min(8, Runtime.getRuntime().availableProcessors())));
		}
		
		StdSchedulerFactory schedFact = new org.quartz.impl.StdSchedulerFactory(props);
		
		final Scheduler scheduler = schedFact.getScheduler();
		scheduler.setJobFactory(autowiringSpringBeanJobFactory);
		
		upgradeService.registerListener(new UpgradeServiceListener() {
			@Override
			public void onUpgradeComplete() {
				try {
					scheduler.getListenerManager().addTriggerListener(new AppJobTriggerListener());
					scheduler.start();
				} catch (SchedulerException e) {
					throw new IllegalStateException(e.getMessage(), e);
				}
			}
			
			@Override
			public void onUpgradeFinished() {
			}
		});
		return scheduler;
	}
}
