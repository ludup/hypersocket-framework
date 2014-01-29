package com.hypersocket.scheduler;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;

public interface SchedulerService {

	String scheduleNow(Class<? extends Job> clz, JobDataMap data, int interval) throws SchedulerException;

	String scheduleNow(Class<? extends Job> clz, JobDataMap data, int interval,
			int repeat) throws SchedulerException;
	
	String scheduleNow(Class<? extends Job> clz, JobDataMap data)
			throws SchedulerException;

	String scheduleAt(Class<? extends Job> clz, JobDataMap data, Date start)
			throws SchedulerException;
	
	String scheduleAt(Class<? extends Job> clz, JobDataMap data, Date start, int interval)
			throws SchedulerException;
	
	String scheduleAt(Class<? extends Job> clz, JobDataMap data, Date start, int interval, int repeat)
			throws SchedulerException;

	String scheduleIn(Class<? extends Job> clz, JobDataMap data, int minutes)
			throws SchedulerException;

	String scheduleIn(Class<? extends Job> clz, JobDataMap data, int minutes,
			int interval) throws SchedulerException;

	String scheduleIn(Class<? extends Job> clz, JobDataMap data, int minutes,
			int interval, int repeat) throws SchedulerException;

	void rescheduleIn(String scheduleId, int minutes, int interval, int repeat)
			throws SchedulerException;

	void rescheduleIn(String scheduleId, int minutes, int interval)
			throws SchedulerException;

	void rescheduleIn(String scheduleId, int minutes) throws SchedulerException;

	void rescheduleAt(String scheduleId, Date time, int interval, int repeat)
			throws SchedulerException;

	void rescheduleAt(String scheduleId, Date time, int interval)
			throws SchedulerException;

	void rescheduleAt(String scheduleId, Date time) throws SchedulerException;

	void rescheduleNow(String scheduleId) throws SchedulerException;

	void rescheduleNow(String scheduleId, int interval)
			throws SchedulerException;

	void rescheduleNow(String scheduleId, int interval, int repeat)
			throws SchedulerException;

	void cancelNow(String scheduleId) throws SchedulerException;

}
