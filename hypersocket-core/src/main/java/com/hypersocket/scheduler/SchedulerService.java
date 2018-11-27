package com.hypersocket.scheduler;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.tables.ColumnSort;

public interface SchedulerService {

	public static final String RESOURCE_BUNDLE = "SchedulerService";

	void scheduleNow(Class<? extends Job> clz, String scheduleId, JobDataMap data, long interval)
			throws SchedulerException;

	void scheduleNow(Class<? extends Job> clz, String scheduleId, JobDataMap data, long interval,
			int repeat) throws SchedulerException;

	void scheduleNow(Class<? extends Job> clz, String scheduleId, JobDataMap data)
			throws SchedulerException;

	void scheduleAt(Class<? extends Job> clz, String scheduleId, JobDataMap data, Date start)
			throws SchedulerException;

	void scheduleAt(Class<? extends Job> clz, String scheduleId, JobDataMap data, Date start,
			long interval) throws SchedulerException;

	void scheduleAt(Class<? extends Job> clz, String scheduleId, JobDataMap data, Date start,
			long interval, int repeat) throws SchedulerException;

	void scheduleIn(Class<? extends Job> clz, String scheduleId, JobDataMap data, int millis)
			throws SchedulerException;

	void scheduleIn(Class<? extends Job> clz, String scheduleId, JobDataMap data, int millis,
			long interval) throws SchedulerException;

	void scheduleIn(Class<? extends Job> clz, String scheduleId, JobDataMap data, int millis,
			long interval, int repeat) throws SchedulerException;
	
	void scheduleIn(Class<? extends Job> clz, String scheduleId, JobDataMap data, int millis,
			long interval, Date ends) throws SchedulerException;

	void scheduleNow(Class<? extends Job> clz, String scheduleId, JobDataMap data, long interval,
			int repeat, Date ends) throws SchedulerException;

	void scheduleAt(Class<? extends Job> clz, String scheduleId, JobDataMap data, Date start,
			long interval, int repeat, Date ends) throws SchedulerException;

	void rescheduleIn(String scheduleId, int millis, long interval, int repeat)
			throws SchedulerException, NotScheduledException;

	void rescheduleIn(String scheduleId, int millis, long interval)
			throws SchedulerException, NotScheduledException;

	void rescheduleIn(String scheduleId, int millis) throws SchedulerException,
			NotScheduledException;

	void rescheduleAt(String scheduleId, Date time, long interval, int repeat)
			throws SchedulerException, NotScheduledException;

	void rescheduleAt(String scheduleId, Date time, long interval, int repeat,
			Date end) throws SchedulerException, NotScheduledException;

	void rescheduleAt(String scheduleId, Date time, long interval)
			throws SchedulerException, NotScheduledException;

	void rescheduleAt(String scheduleId, Date time) throws SchedulerException,
			NotScheduledException;

	void rescheduleNow(String scheduleId) throws SchedulerException,
			NotScheduledException;

	void rescheduleNow(String scheduleId, long interval)
			throws SchedulerException, NotScheduledException;

	void rescheduleNow(String scheduleId, long interval, int repeat)
			throws SchedulerException, NotScheduledException;

	void cancelNow(String scheduleId) throws SchedulerException;

	Date getNextSchedule(String string) throws SchedulerException, NotScheduledException;

	Date getPreviousSchedule(String string) throws SchedulerException, NotScheduledException;

	void rescheduleNow(String scheduleId, long interval, int repeat, Date end)
			throws SchedulerException, NotScheduledException;

	public boolean jobExists(String scheduleId) throws SchedulerException;
	
	public boolean jobDoesNotExists(String scheduleId) throws SchedulerException;

	List<SchedulerResource> searchResources(Realm currentRealm, String searchColumn, String searchPattern, int start, int length,
			ColumnSort[] sorting) throws AccessDeniedException ;

	Long getResourceCount(Realm currentRealm, String searchColumn, String searchPattern) throws AccessDeniedException;

	Collection<SchedulerResource> getResources(Realm currentRealm) throws SchedulerException, AccessDeniedException;

	SchedulerResource getResourceById(String id) throws SchedulerException, NotScheduledException, AccessDeniedException;

	void deleteResource(SchedulerResource resource) throws SchedulerException, AccessDeniedException;

	List<SchedulerResource> getResourcesByIds(String[] ids) throws SchedulerException, AccessDeniedException;

	void deleteResources(List<SchedulerResource> messageResources) throws SchedulerException, AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate(SchedulerResource resource);

	Collection<PropertyCategory> getPropertyTemplate();

}
