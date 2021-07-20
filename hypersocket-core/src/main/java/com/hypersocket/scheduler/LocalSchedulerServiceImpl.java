package com.hypersocket.scheduler;

import java.util.Properties;

import javax.annotation.PostConstruct;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.impl.StdSchedulerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.i18n.I18NService;
import com.hypersocket.upgrade.UpgradeService;
import com.hypersocket.upgrade.UpgradeServiceListener;

@Service
public class LocalSchedulerServiceImpl extends AbstractSchedulerServiceImpl implements LocalSchedulerService {

	@Autowired
	private I18NService i18nService;
	
	@Autowired 
	private AutowiringSpringBeanJobFactory autowiringSpringBeanJobFactory;
	
	@Autowired
	private UpgradeService upgradeService;
	
	@PostConstruct
	void setup() {
		i18nService.registerBundle(RESOURCE_BUNDLE);
	}
	
	protected Scheduler configureScheduler() throws SchedulerException {

		Properties props = new Properties();
		props.setProperty("org.quartz.scheduler.instanceName", "LocalScheduler");
		props.setProperty("org.quartz.scheduler.instanceId", "LOCAL_JOBS");
		props.setProperty("org.quartz.threadPool.class", "com.hypersocket.scheduler.LocalThreadPool");
		props.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore");
		props.setProperty("org.quartz.threadPool.threadCount", "50");
		
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
			
			@Override
			public void onUpgradeFinished() {
			}
		});
		return scheduler;
	}
}
