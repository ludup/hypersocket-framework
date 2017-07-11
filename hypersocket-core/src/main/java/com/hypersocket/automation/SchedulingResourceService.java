package com.hypersocket.automation;

import java.util.Date;

import org.quartz.Job;
import org.quartz.SchedulerException;

import com.hypersocket.resource.RealmResource;

public interface SchedulingResourceService {

	<T extends RealmResource> void unschedule(T resource) throws SchedulerException;

	<T extends RealmResource> void schedule(T resource, Date startDate, String startTime, Date endDate, String endTime,
			AutomationRepeatType repeatType, int repeatValue, Class<? extends Job> clz);

}
