package com.hypersocket.scheduler;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.utils.HypersocketUtils;

public abstract class AbstractSchedulerServiceImpl implements SchedulerService {

	static Logger log = LoggerFactory.getLogger(AbstractSchedulerServiceImpl.class);

	Scheduler scheduler;

	@Autowired
	I18NService i18nService;

	@Autowired
	AutowiringSpringBeanJobFactory autowireJobFactory;

	@Autowired
	EventService eventService;

	@PostConstruct
	private void postConstruct() throws SchedulerException {
		
		scheduler = configureScheduler();
		i18nService.registerBundle(RESOURCE_BUNDLE);
	}
	
	protected abstract Scheduler configureScheduler() throws SchedulerException;
	
	@Override
	public void scheduleNow(Class<? extends Job> clz, String scheduleId, JobDataMap data)
			throws SchedulerException {
		scheduleNow(clz,scheduleId, data, 0, 0);
	}

	@Override
	public void scheduleNow(Class<? extends Job> clz, String scheduleId, JobDataMap data,
			long interval, int repeat) throws SchedulerException {
		schedule(clz, scheduleId, data, null, interval, repeat, null);
	}

	@Override
	public void scheduleNow(Class<? extends Job> clz, String scheduleId, JobDataMap data,
			long interval) throws SchedulerException {
		scheduleNow(clz, scheduleId, data, interval,
				SimpleTrigger.REPEAT_INDEFINITELY);
	}

	@Override
	public void scheduleNow(Class<? extends Job> clz, String scheduleId, JobDataMap data,
			long interval, int repeat, Date ends) throws SchedulerException {
		schedule(clz, scheduleId, data, null, interval, repeat, null);
	}

	@Override
	public void scheduleAt(Class<? extends Job> clz, String scheduleId, JobDataMap data,
			Date start) throws SchedulerException {
		schedule(clz, scheduleId, data, start, 0, 0, null);
	}

	@Override
	public void scheduleAt(Class<? extends Job> clz, String scheduleId, JobDataMap data,
			Date start, long interval) throws SchedulerException {
		schedule(clz, scheduleId, data, start, interval,
				SimpleTrigger.REPEAT_INDEFINITELY, null);
	}

	@Override
	public void scheduleAt(Class<? extends Job> clz, String scheduleId, JobDataMap data,
			Date start, long interval, int repeat) throws SchedulerException {
		schedule(clz, scheduleId, data, start, interval, repeat, null);
	}

	@Override
	public void scheduleAt(Class<? extends Job> clz, String scheduleId, JobDataMap data,
			Date start, long interval, int repeat, Date ends)
			throws SchedulerException {
		schedule(clz, scheduleId, data, start, interval, repeat, ends);
	}

	@Override
	public void scheduleIn(Class<? extends Job> clz, String scheduleId, JobDataMap data,
			int millis) throws SchedulerException {
		scheduleIn(clz, scheduleId, data, millis, 0,
				SimpleTrigger.REPEAT_INDEFINITELY);
	}

	@Override
	public void scheduleIn(Class<? extends Job> clz, String scheduleId, JobDataMap data,
			int millis, long interval) throws SchedulerException {
		schedule(clz, scheduleId, data, DateBuilder.futureDate(millis,
				DateBuilder.IntervalUnit.MILLISECOND), interval,
				SimpleTrigger.REPEAT_INDEFINITELY, null);
	}

	@Override
	public void scheduleIn(Class<? extends Job> clz, String scheduleId, JobDataMap data,
			int millis, long interval, Date ends) throws SchedulerException {
		schedule(clz, scheduleId, data, DateBuilder.futureDate(millis,
				DateBuilder.IntervalUnit.MILLISECOND), interval,
				SimpleTrigger.REPEAT_INDEFINITELY, ends);
	}

	@Override
	public void scheduleIn(Class<? extends Job> clz, String scheduleId, JobDataMap data,
			int millis, long interval, int repeat) throws SchedulerException {
		scheduleAt(clz, scheduleId, data, DateBuilder.futureDate(millis,
				DateBuilder.IntervalUnit.MILLISECOND), interval, repeat);
	}

	@Override
	public void rescheduleIn(String scheduleId, int millis, long interval,
			int repeat) throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, DateBuilder.futureDate(millis,
				DateBuilder.IntervalUnit.MILLISECOND), interval, repeat, null);
	}

	@Override
	public void rescheduleIn(String scheduleId, int millis, long interval)
			throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, DateBuilder.futureDate(millis,
				DateBuilder.IntervalUnit.MILLISECOND), interval, SimpleTrigger.REPEAT_INDEFINITELY, null);
	}

	@Override
	public void rescheduleIn(String scheduleId, int millis)
			throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, DateBuilder.futureDate(millis,
				DateBuilder.IntervalUnit.MILLISECOND), 0, 0, null);
	}

	@Override
	public void rescheduleAt(String scheduleId, Date time, long interval,
			int repeat) throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, time, interval, repeat, null);
	}

	@Override
	public void rescheduleAt(String scheduleId, Date time, long interval,
			int repeat, Date end) throws SchedulerException,
			NotScheduledException {
		reschedule(scheduleId, time, interval, repeat, end);
	}

	@Override
	public void rescheduleAt(String scheduleId, Date time, long interval)
			throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, time, interval,
				SimpleTrigger.REPEAT_INDEFINITELY, null);
	}

	@Override
	public void rescheduleAt(String scheduleId, Date time)
			throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, time, 0, 0, null);
	}

	@Override
	public void rescheduleNow(String scheduleId) throws SchedulerException,
			NotScheduledException {
		reschedule(scheduleId, null, 0, 0, null);
	}

	@Override
	public void rescheduleNow(String scheduleId, long interval)
			throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, null, interval,
				SimpleTrigger.REPEAT_INDEFINITELY, null);
	}

	@Override
	public void rescheduleNow(String scheduleId, long interval, int repeat)
			throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, null, interval, repeat, null);
	}

	@Override
	public void rescheduleNow(String scheduleId, long interval, int repeat,
			Date end) throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, null, interval, repeat, end);
	}

	protected void schedule(Class<? extends Job> clz, String scheduleId, JobDataMap data,
			Date start, long interval, int repeat, Date end)
			throws SchedulerException {

		if(scheduleId == null){
			throw new IllegalArgumentException("Schedule id cannot be null");
		}
		
		if(jobExists(scheduleId)){
			if (log.isInfoEnabled()){
				log.info(String.format("The job with identity %s already exists will not be scheduled again !!!!!!!.", scheduleId));
			}
		}

		if (log.isInfoEnabled()) {
			log.info("Scheduling job " 
					+ clz.getSimpleName()
					+ " with id "
					+ scheduleId
					+ " to start "
					+ (start == null ? "now" : "at "
							+ HypersocketUtils.formatDateTime(start))
					+ " with interval of "
					+ (interval / 60000)
					+ " minutes and repeat "
					+ (repeat >= 0 ? (repeat / 60000) + " time(s)"
							: "indefinately")
					+ (end != null ? " until "
							+ HypersocketUtils.formatDateTime(end) : ""));
		}
		JobDetail job = JobBuilder.newJob(clz).withIdentity(scheduleId).build();

		Trigger trigger = createTrigger(data, start, interval, repeat, end);
		
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(
				"started",
				(start == null ? HypersocketUtils.formatDate(new Date(),
						"yyyy/MM/dd HH:mm") : HypersocketUtils.formatDate(
						start, "yyyy/MM/dd HH:mm")));
		properties.put("intervals", String.valueOf((interval / 60000)));
		try {
			scheduler.scheduleJob(job, trigger);
		} catch (Exception e) {
			log.error("Error in create resource for schedule ", e);
		}
	}

	@Override
	public boolean jobExists(String scheduleId) throws SchedulerException {
		return scheduler.checkExists(new JobKey(scheduleId));
	}
	
	@Override
	public boolean jobDoesNotExists(String scheduleId) throws SchedulerException {
		return !jobExists(scheduleId);
	}

	protected Trigger createTrigger(JobDataMap data, Date start, long interval,
			int repeat, Date end) {

		TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();

		if (data != null) {
			triggerBuilder.usingJobData(data);
		}
		if (interval > 0) {
			SimpleScheduleBuilder schedule = SimpleScheduleBuilder
					.simpleSchedule();
			schedule.withIntervalInMilliseconds(interval).withRepeatCount(
					repeat);
			triggerBuilder.withSchedule(schedule);
		}

		if (start != null) {
			triggerBuilder.startAt(start);
		} else {
			triggerBuilder.startNow();
		}

		if (end != null) {
			triggerBuilder.endAt(end);
		}

		return triggerBuilder.build();
	}

	protected void reschedule(String id, Date start, long interval, int repeat,
			Date end) throws SchedulerException, NotScheduledException {

		if (log.isInfoEnabled()) {
			log.info("Rescheduling job with id "
					+ id
					+ " to start "
					+ (start == null ? "now" : "at "
							+ HypersocketUtils.formatDateTime(start))
					+ " with interval of "
					+ interval
					+ " and repeat "
					+ (repeat >= 0 ? repeat : "indefinatley")
					+ (end != null ? " until "
							+ HypersocketUtils.formatDateTime(end) : ""));
		}
		TriggerKey triggerKey = scheduler.getTriggersOfJob(new JobKey(id)).get(0).getKey();

		if (triggerKey != null) {

			Trigger oldTrigger = scheduler.getTrigger(triggerKey);
			Trigger trigger = createTrigger(oldTrigger.getJobDataMap(), start,
					interval, repeat, end);
			scheduler.rescheduleJob(triggerKey, trigger);
			Map<String, String> properties = new HashMap<String, String>();
			properties.put(
					"started",
					(start == null ? HypersocketUtils.formatDate(new Date(),
							"yyyy/MM/dd HH:mm") : HypersocketUtils.formatDate(
							start, "yyyy/MM/dd HH:mm")));
			properties.put("intervals", String.valueOf((interval / 60000)));

		} else {
			cancelNow(id);
			throw new NotScheduledException();
		}
	}

	@Override
	public void cancelNow(String id) throws SchedulerException {

		if (log.isInfoEnabled()) {
			log.info("Cancelling job with id " + id);
		}

		JobKey jobKey = new JobKey(id);

		if (scheduler.checkExists(jobKey)) {
			scheduler.deleteJob(jobKey);
		}
	}

	@Override
	public Date getNextSchedule(String id) throws SchedulerException, NotScheduledException {
		List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(new JobKey(id));
		if(triggersOfJob.isEmpty()) {
			throw new NotScheduledException();
		}
		Trigger trigger = triggersOfJob.get(0);
		return trigger.getNextFireTime();
	}

	@Override
	public Date getPreviousSchedule(String id) throws SchedulerException, NotScheduledException {
		List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(new JobKey(id));
		if(triggersOfJob.isEmpty()) {
			throw new NotScheduledException();
		}
		Trigger trigger = triggersOfJob.get(0);
		return trigger.getPreviousFireTime();
	}
}
