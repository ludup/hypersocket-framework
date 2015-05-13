package com.hypersocket.scheduler;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.scheduler.events.SchedulerResourceCreatedEvent;
import com.hypersocket.scheduler.events.SchedulerResourceDeletedEvent;
import com.hypersocket.scheduler.events.SchedulerResourceEvent;
import com.hypersocket.scheduler.events.SchedulerResourceUpdatedEvent;
import com.hypersocket.utils.HypersocketUtils;

@Service
public class SchedulerServiceImpl extends
		AbstractResourceServiceImpl<SchedulerResource> implements
		SchedulerService {

	static Logger log = LoggerFactory.getLogger(SchedulerServiceImpl.class);

	Scheduler scheduler;
	
	@Autowired
	I18NService i18nService;

	@Autowired
	AutowiringSpringBeanJobFactory autowireJobFactory;

	@Autowired
	SchedulerResourceRepository repository;
	
	@Autowired
	EntityResourcePropertyStore entityPropertyStore;
	
	@Autowired
	EventService eventService;

	Map<String, JobKey> jobKeys = new HashMap<String, JobKey>();
	Map<String, TriggerKey> triggerKeys = new HashMap<String, TriggerKey>();

	@PostConstruct
	private void postConstruct() throws SchedulerException {
		scheduler = StdSchedulerFactory.getDefaultScheduler();
		scheduler.setJobFactory(autowireJobFactory);
		scheduler.start();
		repository.loadPropertyTemplates("schedulerResourceTemplate.xml");
		i18nService.registerBundle(RESOURCE_BUNDLE);
		eventService.registerEvent(
				SchedulerResourceEvent.class, RESOURCE_BUNDLE,
				this);
		eventService.registerEvent(
				SchedulerResourceCreatedEvent.class, RESOURCE_BUNDLE,
				this);
		eventService.registerEvent(
				SchedulerResourceUpdatedEvent.class, RESOURCE_BUNDLE,
				this);
		eventService.registerEvent(
				SchedulerResourceDeletedEvent.class, RESOURCE_BUNDLE,
				this);
		entityPropertyStore.registerResourceService(SchedulerResource.class, this);
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
		return schedule(clz, data, null, interval, repeat, null);
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
			Date start, int interval, int repeat, Date ends)
			throws SchedulerException {
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
		reschedule(scheduleId, DateBuilder.futureDate(millis,
				DateBuilder.IntervalUnit.MILLISECOND), interval, repeat, null);
	}

	@Override
	public void rescheduleIn(String scheduleId, int millis, int interval)
			throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, DateBuilder.futureDate(millis,
				DateBuilder.IntervalUnit.MILLISECOND), interval, 0, null);
	}

	@Override
	public void rescheduleIn(String scheduleId, int millis)
			throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, DateBuilder.futureDate(millis,
				DateBuilder.IntervalUnit.MILLISECOND), 0, 0, null);
	}

	@Override
	public void rescheduleAt(String scheduleId, Date time, int interval,
			int repeat) throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, time, interval, repeat, null);
	}

	@Override
	public void rescheduleAt(String scheduleId, Date time, int interval,
			int repeat, Date end) throws SchedulerException,
			NotScheduledException {
		reschedule(scheduleId, time, interval, repeat, end);
	}

	@Override
	public void rescheduleAt(String scheduleId, Date time, int interval)
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
	public void rescheduleNow(String scheduleId, int interval)
			throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, null, interval,
				SimpleTrigger.REPEAT_INDEFINITELY, null);
	}

	@Override
	public void rescheduleNow(String scheduleId, int interval, int repeat)
			throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, null, interval, repeat, null);
	}

	@Override
	public void rescheduleNow(String scheduleId, int interval, int repeat,
			Date end) throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, null, interval, repeat, end);
	}

	protected String schedule(Class<? extends Job> clz, JobDataMap data,
			Date start, int interval, int repeat, Date end)
			throws SchedulerException {

		String uuid = UUID.randomUUID().toString();

		if (log.isInfoEnabled()) {
			log.info("Scheduling job with id "
					+ uuid
					+ " to start "
					+ (start == null ? "now" : "at "
							+ HypersocketUtils.formatDate(start))
					+ " with interval of "
					+ (interval / 60000)
					+ " minutes and repeat "
					+ (repeat >= 0 ? (repeat / 60000) + " time(s)"
							: "indefinately")
					+ (end != null ? " until "
							+ HypersocketUtils.formatDate(end) : ""));
		}
		JobDetail job = JobBuilder.newJob(clz).build();
		jobKeys.put(uuid, job.getKey());

		Trigger trigger = createTrigger(data, start, interval, repeat, end);

		triggerKeys.put(uuid, trigger.getKey());
		scheduler.scheduleJob(job, trigger);
		
		try {
			createResource(uuid, getCurrentRealm(), new HashMap<String,String>());
		} catch (ResourceCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return uuid;
	}

	protected Trigger createTrigger(JobDataMap data, Date start, int interval,
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

	protected void reschedule(String id, Date start, int interval, int repeat,
			Date end) throws SchedulerException, NotScheduledException {

		if (log.isInfoEnabled()) {
			log.info("Rescheduling job with id "
					+ id
					+ " to start "
					+ (start == null ? "now" : "at "
							+ HypersocketUtils.formatDate(start))
					+ " with interval of "
					+ interval
					+ " and repeat "
					+ (repeat >= 0 ? repeat : "indefinatley")
					+ (end != null ? " until "
							+ HypersocketUtils.formatDate(end) : ""));
		}
		TriggerKey triggerKey = triggerKeys.get(id);

		if (scheduler.checkExists(triggerKey)) {

			Trigger oldTrigger = scheduler.getTrigger(triggerKey);
			Trigger trigger = createTrigger(oldTrigger.getJobDataMap(), start,
					interval, repeat, end);
			scheduler.rescheduleJob(triggerKey, trigger);
			triggerKeys.put(id, trigger.getKey());

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

		triggerKeys.remove(id);
		JobKey jobKey = jobKeys.remove(id);

		if (scheduler.checkExists(jobKey)) {
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

	@Override
	public void getSheduledJobs() throws SchedulerException {
		System.out.println("call for jobs");
		for (String group : scheduler.getJobGroupNames()) {
			System.out.println("Registered groups :" + group);
			// enumerate each job in group
			for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher
					.jobGroupEquals(group))) {
				System.out.println("++++++++++++++++++++++++++++");
				System.out.println("Found job identified by: " + jobKey
						+ " , name :" + jobKey.getName());
				JobDetail detail = scheduler.getJobDetail(jobKey);
				System.out.println(" Job detail :" + detail);
				List<? extends Trigger> triggersOfJob = scheduler
						.getTriggersOfJob(jobKey);
				for (Trigger trigger : triggersOfJob) {
					System.out.println("Triger.........");
					System.out.println("Data class :"
							+ trigger.getJobDataMap().getClass().getName());
					System.out.println("Job Data : ");
					if (trigger.getJobDataMap() instanceof PermissionsAwareJobData) {
						PermissionsAwareJobData data = (PermissionsAwareJobData) trigger
								.getJobDataMap();
						System.out.println("Name :" + data.getName());
						System.out.println("Job type :" + data.getJobType());
						/*
						 * for(String key :trigger.getJobDataMap().getKeys()){
						 * System.out.println("Key : "+key); }
						 */

					} else {
						for (String key : trigger.getJobDataMap().getKeys()) {
							System.out.println("Key : " + key);
						}
					}
					System.out.println("Last fire time :"
							+ trigger.getPreviousFireTime());
					System.out.println("Next fire time :"
							+ trigger.getNextFireTime());
					System.out.println(trigger.toString());

				}
			}
		}

	}

	@Override
	public SchedulerResource updateResource(SchedulerResource resourceById,
			String name, Map<String, String> properties)
			throws ResourceChangeException, AccessDeniedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SchedulerResource createResource(String name, Realm realm,
			Map<String, String> properties) throws ResourceCreationException,
			AccessDeniedException {

		SchedulerResource resource = new SchedulerResource();
		resource.setName(name);
		resource.setRealm(realm);
		/**
		 * Set any additional fields on your resource here before calling
		 * createResource.
		 * 
		 * Remember to fill in the fire*Event methods to ensure events are fired
		 * for all operations.
		 */
		createResource(resource, properties);
		return resource;
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate()
			throws AccessDeniedException {
		return repository.getPropertyCategories(null);
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate(
			SchedulerResource resource) throws AccessDeniedException {
		return repository.getPropertyCategories(resource);
	}

	@Override
	protected AbstractResourceRepository<SchedulerResource> getRepository() {
		return repository;
	}

	@Override
	protected String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public Class<? extends PermissionType> getPermissionType() {
		return SchedulerResourcePermission.class;
	}

	@Override
	protected void fireResourceCreationEvent(SchedulerResource resource) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void fireResourceCreationEvent(SchedulerResource resource,
			Throwable t) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void fireResourceUpdateEvent(SchedulerResource resource) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void fireResourceUpdateEvent(SchedulerResource resource,
			Throwable t) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void fireResourceDeletionEvent(SchedulerResource resource) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void fireResourceDeletionEvent(SchedulerResource resource,
			Throwable t) {
		// TODO Auto-generated method stub

	}

}
