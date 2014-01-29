package com.hypersocket.scheduler;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.quartz.DateBuilder;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SchedulerServiceImpl implements SchedulerService {

	static Logger log = LoggerFactory.getLogger(SchedulerServiceImpl.class);

	Scheduler scheduler;

	@Autowired
	AutowiringSpringBeanJobFactory autowireJobFactory;

	Map<String, JobKey> jobKeys = new HashMap<String, JobKey>();
	Map<String, TriggerKey> triggerKeys = new HashMap<String, TriggerKey>();

	@PostConstruct
	public void postConstruct() throws SchedulerException {
		scheduler = StdSchedulerFactory.getDefaultScheduler();
		scheduler.setJobFactory(autowireJobFactory);
		scheduler.start();
	}

	@Override
	public String scheduleNow(Class<? extends Job> clz, JobDataMap data)
			throws SchedulerException {
		return scheduleNow(clz, data, 0, 0);
	}

	@Override
	public String scheduleNow(Class<? extends Job> clz, JobDataMap data,
			int interval, int repeat) throws SchedulerException {
		return schedule(clz, data, null, interval, repeat);
	}

	@Override
	public String scheduleNow(Class<? extends Job> clz, JobDataMap data,
			int interval) throws SchedulerException {
		return scheduleNow(clz, data, interval,
				SimpleTrigger.REPEAT_INDEFINITELY);
	}

	@Override
	public String scheduleAt(Class<? extends Job> clz, JobDataMap data,
			Date start) throws SchedulerException {
		return schedule(clz, data, start, 0, 0);
	}

	@Override
	public String scheduleAt(Class<? extends Job> clz, JobDataMap data,
			Date start, int interval) throws SchedulerException {
		return schedule(clz, data, start, interval,
				SimpleTrigger.REPEAT_INDEFINITELY);
	}

	@Override
	public String scheduleAt(Class<? extends Job> clz, JobDataMap data,
			Date start, int interval, int repeat) throws SchedulerException {
		return schedule(clz, data, start, interval, repeat);
	}

	@Override
	public String scheduleIn(Class<? extends Job> clz, JobDataMap data,
			int minutes) throws SchedulerException {
		return scheduleIn(clz, data, minutes, 0,
				SimpleTrigger.REPEAT_INDEFINITELY);
	}

	@Override
	public String scheduleIn(Class<? extends Job> clz, JobDataMap data,
			int minutes, int interval) throws SchedulerException {
		return schedule(clz, data, DateBuilder.futureDate(minutes,
				DateBuilder.IntervalUnit.MINUTE), interval,
				SimpleTrigger.REPEAT_INDEFINITELY);
	}

	@Override
	public String scheduleIn(Class<? extends Job> clz, JobDataMap data,
			int minutes, int interval, int repeat) throws SchedulerException {
		return scheduleAt(clz, data, DateBuilder.futureDate(minutes,
				DateBuilder.IntervalUnit.MINUTE), interval, repeat);
	}

	@Override
	public void rescheduleIn(String scheduleId, int minutes, int interval, int repeat) throws SchedulerException {
		reschedule(scheduleId, DateBuilder.futureDate(minutes, DateBuilder.IntervalUnit.MINUTE), interval, repeat);
	}
	
	@Override
	public void rescheduleIn(String scheduleId, int minutes, int interval) throws SchedulerException {
		reschedule(scheduleId, DateBuilder.futureDate(minutes, DateBuilder.IntervalUnit.MINUTE), interval, 0);
	}
	
	@Override
	public void rescheduleIn(String scheduleId, int minutes) throws SchedulerException {
		reschedule(scheduleId, DateBuilder.futureDate(minutes, DateBuilder.IntervalUnit.MINUTE), 0, 0);
	}

	@Override
	public void rescheduleAt(String scheduleId, Date time, int interval, int repeat) throws SchedulerException {
		reschedule(scheduleId, time, interval, repeat);
	}
	
	@Override
	public void rescheduleAt(String scheduleId, Date time, int interval) throws SchedulerException {
		reschedule(scheduleId, time, interval, SimpleTrigger.REPEAT_INDEFINITELY);
	}
	
	@Override
	public void rescheduleAt(String scheduleId, Date time) throws SchedulerException {
		reschedule(scheduleId, time, 0, 0);
	}
	
	@Override
	public void rescheduleNow(String scheduleId) throws SchedulerException {
		reschedule(scheduleId, null, 0, 0);
	}
	
	@Override
	public void rescheduleNow(String scheduleId, int interval) throws SchedulerException {
		reschedule(scheduleId, null, interval, SimpleTrigger.REPEAT_INDEFINITELY);
	}
	
	@Override
	public void rescheduleNow(String scheduleId, int interval, int repeat) throws SchedulerException {
		reschedule(scheduleId, null, interval, repeat);
	}
	
	protected String schedule(Class<? extends Job> clz, JobDataMap data,
			Date start, int interval, int repeat) throws SchedulerException {

		String uuid = UUID.randomUUID().toString();

		JobDetail job = JobBuilder.newJob(clz).build();
		jobKeys.put(uuid, job.getKey());

		Trigger trigger = createTrigger(data, start, interval, repeat);

		triggerKeys.put(uuid, trigger.getKey());
		scheduler.scheduleJob(job, trigger);
		return uuid;
	}

	protected Trigger createTrigger(JobDataMap data, Date start, int interval,
			int repeat) {

		TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
		
		if(data!=null) {
			triggerBuilder.usingJobData(data);
		}
				
		if (interval > 0) {
			SimpleScheduleBuilder schedule = SimpleScheduleBuilder
					.simpleSchedule();
			schedule.withIntervalInMilliseconds(interval).withRepeatCount(repeat);
			triggerBuilder.withSchedule(schedule);
		}

		if (start != null) {
			triggerBuilder.startAt(start);
		} else {
			triggerBuilder.startNow();
		}

		return triggerBuilder.build();
	}

	protected void reschedule(String id, Date start, int interval, int repeat)
			throws SchedulerException {

		TriggerKey triggerKey = triggerKeys.get(id);

		Trigger trigger = createTrigger(scheduler.getTrigger(triggerKey)
				.getJobDataMap(), start, interval, repeat);

		scheduler.rescheduleJob(triggerKey, trigger);

		triggerKeys.put(id, trigger.getKey());
	}

	@Override
	public void cancelNow(String id) throws SchedulerException {
		
		triggerKeys.remove(id);
		JobKey jobKey = jobKeys.remove(id);
		
		if(scheduler.checkExists(jobKey)) {
			scheduler.deleteJob(jobKey);
		}
	}

}
