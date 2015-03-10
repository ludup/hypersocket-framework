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

import com.hypersocket.utils.HypersocketUtils;

@Service
public class SchedulerServiceImpl implements SchedulerService {

	static Logger log = LoggerFactory.getLogger(SchedulerServiceImpl.class);

	Scheduler scheduler;

	@Autowired
	AutowiringSpringBeanJobFactory autowireJobFactory;

	Map<String, JobKey> jobKeys = new HashMap<String, JobKey>();
	Map<String, TriggerKey> triggerKeys = new HashMap<String, TriggerKey>();

	@PostConstruct
	private void postConstruct() throws SchedulerException {
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
		return schedule(clz, data, null, interval, repeat, null);
	}

	@Override
	public String scheduleNow(Class<? extends Job> clz, JobDataMap data,
			int interval) throws SchedulerException {
		return scheduleNow(clz, data, interval,
				SimpleTrigger.REPEAT_INDEFINITELY);
	}

	@Override
	public String scheduleNow(Class<? extends Job> clz, JobDataMap data,
			int interval, int repeat, Date ends) throws SchedulerException {
		return schedule(clz, data, null,  interval,
				repeat, null);
	}
	
	@Override
	public String scheduleAt(Class<? extends Job> clz, JobDataMap data,
			Date start) throws SchedulerException {
		return schedule(clz, data, start, 0, 0, null);
	}

	@Override
	public String scheduleAt(Class<? extends Job> clz, JobDataMap data,
			Date start, int interval) throws SchedulerException {
		return schedule(clz, data, start, interval,
				SimpleTrigger.REPEAT_INDEFINITELY, null);
	}

	@Override
	public String scheduleAt(Class<? extends Job> clz, JobDataMap data,
			Date start, int interval, int repeat) throws SchedulerException {
		return schedule(clz, data, start, interval, repeat, null);
	}

	@Override
	public String scheduleAt(Class<? extends Job> clz, JobDataMap data,
			Date start, int interval, int repeat, Date ends) throws SchedulerException {
		return schedule(clz, data, start, interval, repeat, ends);
	}
	
	@Override
	public String scheduleIn(Class<? extends Job> clz, JobDataMap data,
			int millis) throws SchedulerException {
		return scheduleIn(clz, data, millis, 0,
				SimpleTrigger.REPEAT_INDEFINITELY);
	}

	@Override
	public String scheduleIn(Class<? extends Job> clz, JobDataMap data,
			int millis, int interval) throws SchedulerException {
		return schedule(clz, data, DateBuilder.futureDate(millis,
				DateBuilder.IntervalUnit.MILLISECOND), interval,
				SimpleTrigger.REPEAT_INDEFINITELY, null);
	}
	
	@Override
	public String scheduleIn(Class<? extends Job> clz, JobDataMap data,
			int millis, int interval, Date ends) throws SchedulerException {
		return schedule(clz, data, DateBuilder.futureDate(millis,
				DateBuilder.IntervalUnit.MILLISECOND), interval,
				SimpleTrigger.REPEAT_INDEFINITELY, ends);
	}

	@Override
	public String scheduleIn(Class<? extends Job> clz, JobDataMap data,
			int millis, int interval, int repeat) throws SchedulerException {
		return scheduleAt(clz, data, DateBuilder.futureDate(millis,
				DateBuilder.IntervalUnit.MILLISECOND), interval, repeat);
	}

	@Override
	public void rescheduleIn(String scheduleId, int millis, int interval,
			int repeat) throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, DateBuilder.futureDate(millis, DateBuilder.IntervalUnit.MILLISECOND), interval, repeat, null);
	}
	
	@Override
	public void rescheduleIn(String scheduleId, int millis, int interval) throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, DateBuilder.futureDate(millis, DateBuilder.IntervalUnit.MILLISECOND), interval, 0, null);
	}
	
	@Override
	public void rescheduleIn(String scheduleId, int millis) throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, DateBuilder.futureDate(millis, DateBuilder.IntervalUnit.MILLISECOND), 0, 0, null);
	}

	@Override
	public void rescheduleAt(String scheduleId, Date time, int interval, int repeat) throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, time, interval, repeat, null);
	}
	
	@Override
	public void rescheduleAt(String scheduleId, Date time, int interval, int repeat, Date end) throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, time, interval, repeat, end);
	}
	
	@Override
	public void rescheduleAt(String scheduleId, Date time, int interval) throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, time, interval, SimpleTrigger.REPEAT_INDEFINITELY, null);
	}
	
	@Override
	public void rescheduleAt(String scheduleId, Date time) throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, time, 0, 0, null);
	}
	
	@Override
	public void rescheduleNow(String scheduleId) throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, null, 0, 0, null);
	}
	
	@Override
	public void rescheduleNow(String scheduleId, int interval) throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, null, interval, SimpleTrigger.REPEAT_INDEFINITELY, null);
	}
	
	@Override
	public void rescheduleNow(String scheduleId, int interval, int repeat) throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, null, interval, repeat, null);
	}
	
	@Override
	public void rescheduleNow(String scheduleId, int interval, int repeat, Date end) throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, null, interval, repeat, end);
	}
	
	protected String schedule(Class<? extends Job> clz, JobDataMap data,
			Date start, int interval, int repeat, Date end) throws SchedulerException {

		String uuid = UUID.randomUUID().toString();

		if(log.isInfoEnabled()) {
			log.info("Scheduling job with id " + uuid + " to start " + (start==null ? "now" : "at " + HypersocketUtils.formatDate(start))
					 + " with interval of " + (interval/60000) + " minutes and repeat " + (repeat >= 0 ? (repeat/60000) + " time(s)" : "indefinately")
					 + (end != null ? " until " + HypersocketUtils.formatDate(end) : ""));
		}
		JobDetail job = JobBuilder.newJob(clz).build();
		jobKeys.put(uuid, job.getKey());

		Trigger trigger = createTrigger(data, start, interval, repeat, end);
		
		triggerKeys.put(uuid, trigger.getKey());
		scheduler.scheduleJob(job, trigger);
		return uuid;
	}

	protected Trigger createTrigger(JobDataMap data, Date start, int interval,
			int repeat, Date end) {

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
		
		if(end!=null) {
			triggerBuilder.endAt(end);
		}

		return triggerBuilder.build();
	}

	protected void reschedule(String id, Date start, int interval, int repeat, Date end)
			throws SchedulerException, NotScheduledException {

		if(log.isInfoEnabled()) {
			log.info("Rescheduling job with id " + id + " to start " + (start==null ? "now" : "at " + HypersocketUtils.formatDate(start))
					 + " with interval of " + interval + " and repeat " + (repeat >= 0 ? repeat : "indefinatley")
					 + (end != null ? " until " + HypersocketUtils.formatDate(end) : ""));
		}
		TriggerKey triggerKey = triggerKeys.get(id);
		
		if(scheduler.checkExists(triggerKey)) {
		
			Trigger oldTrigger = scheduler.getTrigger(triggerKey);
			Trigger trigger = createTrigger(oldTrigger.getJobDataMap(), 
					start, interval, repeat, end);
			scheduler.rescheduleJob(triggerKey, trigger);
			triggerKeys.put(id, trigger.getKey());
			
		} else {
			cancelNow(id);
			throw new NotScheduledException();
		}
	}

	@Override
	public void cancelNow(String id) throws SchedulerException {

		if(log.isInfoEnabled()) {
			log.info("Cancelling job with id " + id);
		}
		
		triggerKeys.remove(id);
		JobKey jobKey = jobKeys.remove(id);

		if(scheduler.checkExists(jobKey)) {
			scheduler.deleteJob(jobKey);
		}
	}
	
	@Override
	public Date getNextSchedule(String id) throws SchedulerException {
		
		Trigger trigger = scheduler.getTrigger(triggerKeys.get(id));
		return trigger.getNextFireTime();
	}
	
	@Override
	public Date getPreviousSchedule(String id) throws SchedulerException {
		
		Trigger trigger = scheduler.getTrigger(triggerKeys.get(id));
		return trigger.getPreviousFireTime();
	}

}
