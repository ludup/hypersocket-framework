package com.hypersocket.scheduler;

import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;

public interface SchedulerService extends
		AbstractResourceService<SchedulerResource> {

	public static final String RESOURCE_BUNDLE = "SchedulerService";

	String scheduleNow(Class<? extends Job> clz, JobDataMap data, int interval)
			throws SchedulerException;

	String scheduleNow(Class<? extends Job> clz, JobDataMap data, int interval,
			int repeat) throws SchedulerException;

	String scheduleNow(Class<? extends Job> clz, JobDataMap data)
			throws SchedulerException;

	String scheduleAt(Class<? extends Job> clz, JobDataMap data, Date start)
			throws SchedulerException;

	String scheduleAt(Class<? extends Job> clz, JobDataMap data, Date start,
			int interval) throws SchedulerException;

	String scheduleAt(Class<? extends Job> clz, JobDataMap data, Date start,
			int interval, int repeat) throws SchedulerException;

	String scheduleIn(Class<? extends Job> clz, JobDataMap data, int millis)
			throws SchedulerException;

	String scheduleIn(Class<? extends Job> clz, JobDataMap data, int millis,
			int interval) throws SchedulerException;

	String scheduleIn(Class<? extends Job> clz, JobDataMap data, int millis,
			int interval, int repeat) throws SchedulerException;

	void rescheduleIn(String scheduleId, int millis, int interval, int repeat)
			throws SchedulerException, NotScheduledException;

	void rescheduleIn(String scheduleId, int millis, int interval)
			throws SchedulerException, NotScheduledException;

	void rescheduleIn(String scheduleId, int millis) throws SchedulerException,
			NotScheduledException;

	void rescheduleAt(String scheduleId, Date time, int interval, int repeat)
			throws SchedulerException, NotScheduledException;

	void rescheduleAt(String scheduleId, Date time, int interval, int repeat,
			Date end) throws SchedulerException, NotScheduledException;

	void rescheduleAt(String scheduleId, Date time, int interval)
			throws SchedulerException, NotScheduledException;

	void rescheduleAt(String scheduleId, Date time) throws SchedulerException,
			NotScheduledException;

	void rescheduleNow(String scheduleId) throws SchedulerException,
			NotScheduledException;

	void rescheduleNow(String scheduleId, int interval)
			throws SchedulerException, NotScheduledException;

	void rescheduleNow(String scheduleId, int interval, int repeat)
			throws SchedulerException, NotScheduledException;

	void cancelNow(String scheduleId) throws SchedulerException;

	Date getNextSchedule(String string) throws SchedulerException;

	Date getPreviousSchedule(String string) throws SchedulerException;

	void rescheduleNow(String scheduleId, int interval, int repeat, Date end)
			throws SchedulerException, NotScheduledException;

	String scheduleIn(Class<? extends Job> clz, JobDataMap data, int millis,
			int interval, Date ends) throws SchedulerException;

	String scheduleNow(Class<? extends Job> clz, JobDataMap data, int interval,
			int repeat, Date ends) throws SchedulerException;

	String scheduleAt(Class<? extends Job> clz, JobDataMap data, Date start,
			int interval, int repeat, Date ends) throws SchedulerException;

	SchedulerResource updateResource(SchedulerResource resource,
			String name, Map<String, String> properties)
			throws ResourceChangeException, AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate()
			throws AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate(SchedulerResource resource)
			throws AccessDeniedException;

	SchedulerResource createResource(String name, String uuid, Realm realm, Map<String, String> properties)
			throws ResourceCreationException, AccessDeniedException;

}
