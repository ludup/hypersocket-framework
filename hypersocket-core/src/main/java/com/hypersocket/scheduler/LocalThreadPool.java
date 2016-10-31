package com.hypersocket.scheduler;

import org.quartz.SchedulerConfigException;

/**
 * It is intended that this be the configured instance of ThreadPool for
 * the local scheduler. Allowing us access to the scheduler instance so we
 * can use our extended SimpleThreadPoolEx implementation that allows us
 * to change the number of threads at runtime. 
 */
public class LocalThreadPool extends SimpleThreadPoolEx {

	static LocalThreadPool instance;
	
	public static LocalThreadPool getInstance() {
		return instance;
	}

	@Override
	public void initialize() throws SchedulerConfigException {
		super.initialize();
		instance = this;
	}

}
