package com.hypersocket.scheduler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
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
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.Sort;
import com.hypersocket.utils.HypersocketUtils;

@Service
public class SchedulerServiceImpl extends
		AbstractResourceServiceImpl<SchedulerResource> implements
		SchedulerService {

	protected SchedulerServiceImpl() {
		super("Schedulers");
	}

	static Logger log = LoggerFactory.getLogger(SchedulerServiceImpl.class);

	Scheduler scheduler;

	@Autowired
	I18NService i18nService;

	@Autowired
	AutowiringSpringBeanJobFactory autowireJobFactory;

	@Autowired
	SchedulerResourceRepository repository;

	@Autowired
	EventService eventService;

	public static Long SEQUENCE_GEN = 0L;
	Map<String, SchedulerResource> cachedResources = new HashMap<String, SchedulerResource>();

	Map<String, JobKey> jobKeys = new HashMap<String, JobKey>();
	Map<String, TriggerKey> triggerKeys = new HashMap<String, TriggerKey>();

	@PostConstruct
	private void postConstruct() throws SchedulerException {
		
		
		File quartzProperties = new File(HypersocketUtils.getConfigDir(), "quartz.properties");
		if(quartzProperties.exists()) {
			System.setProperty("org.quartz.properties", quartzProperties.getAbsolutePath());
		}
		
		scheduler = StdSchedulerFactory.getDefaultScheduler();
		scheduler.setJobFactory(autowireJobFactory);
		scheduler.start();
		
		repository.loadPropertyTemplates("schedulerResourceTemplate.xml");
		i18nService.registerBundle(RESOURCE_BUNDLE);
		eventService.registerEvent(SchedulerResourceEvent.class,
				RESOURCE_BUNDLE, this);
		eventService.registerEvent(SchedulerResourceCreatedEvent.class,
				RESOURCE_BUNDLE, this);
		eventService.registerEvent(SchedulerResourceUpdatedEvent.class,
				RESOURCE_BUNDLE, this);
		eventService.registerEvent(SchedulerResourceDeletedEvent.class,
				RESOURCE_BUNDLE, this);
		repository.getEntityStore().registerResourceService(SchedulerResource.class,
				repository);
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

		if(data==null || !data.containsKey("jobName")) {
			throw new IllegalArgumentException("JobDataMap must be present with at least jobName data key");
		}
		
		String uuid = UUID.randomUUID().toString();

		if (log.isInfoEnabled()) {
			log.info("Scheduling job with id "
					+ uuid
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
		JobDetail job = JobBuilder.newJob(clz).withIdentity(uuid).build();

		jobKeys.put(uuid, job.getKey());

		Trigger trigger = createTrigger(data, start, interval, repeat, end);
		triggerKeys.put(uuid, trigger.getKey());
		
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(
				"started",
				(start == null ? HypersocketUtils.formatDate(new Date(),
						"yyyy/MM/dd HH:mm") : HypersocketUtils.formatDate(
						start, "yyyy/MM/dd HH:mm")));
		properties.put("intervals", String.valueOf((interval / 60000)));
		try {
			createResource(data.getString("jobName"), uuid, getCurrentRealm(), properties);
			scheduler.scheduleJob(job, trigger);
		} catch (ResourceCreationException e) {
			log.error("Error in create resource for schedule ", e);
		} catch (Exception e) {
			log.error("Error in create resource for schedule ", e);
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
							+ HypersocketUtils.formatDateTime(start))
					+ " with interval of "
					+ interval
					+ " and repeat "
					+ (repeat >= 0 ? repeat : "indefinatley")
					+ (end != null ? " until "
							+ HypersocketUtils.formatDateTime(end) : ""));
		}
		TriggerKey triggerKey = triggerKeys.get(id);

		if (scheduler.checkExists(triggerKey)) {

			Trigger oldTrigger = scheduler.getTrigger(triggerKey);
			Trigger trigger = createTrigger(oldTrigger.getJobDataMap(), start,
					interval, repeat, end);
			scheduler.rescheduleJob(triggerKey, trigger);
			triggerKeys.put(id, trigger.getKey());
			Map<String, String> properties = new HashMap<String, String>();
			properties.put(
					"started",
					(start == null ? HypersocketUtils.formatDate(new Date(),
							"yyyy/MM/dd HH:mm") : HypersocketUtils.formatDate(
							start, "yyyy/MM/dd HH:mm")));
			properties.put("intervals", String.valueOf((interval / 60000)));
			try {
				SchedulerResource resource = cachedResources.get(id);
				if(resource==null) {
					throw new IllegalStateException();
				} else {
					updateResource(resource, oldTrigger.getJobDataMap().getString("jobName"), properties);
				}
			} catch (ResourceChangeException e) {
				log.error("Error in update resource for schedule ", e);
			} catch (Exception e) {
				log.error("Error in update resource for schedule ", e);
			}

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
		cachedResources.remove(id);
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
	public SchedulerResource updateResource(SchedulerResource resource,
			String name, Map<String, String> properties)
			throws ResourceChangeException, AccessDeniedException {
		resource.setStarted(resource.getStarted());
		resource.setIntervals(resource.getIntervals());
		return resource;
	}

	@Override
	public SchedulerResource createResource(String name, String uuid, Realm realm,
			Map<String, String> properties) throws ResourceCreationException,
			AccessDeniedException {

		SchedulerResource resource = new SchedulerResource();
		resource.setId(++SEQUENCE_GEN);
		resource.setName(name);
		resource.setStarted(properties.get("started"));
		resource.setIntervals(Integer.parseInt(properties.get("intervals")));
		resource.setRealm(realm);
		resource.setJobId(uuid);
		cachedResources.put(uuid, resource);
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
	public List<SchedulerResource> searchResources(Realm realm, String searchColumn, String search,
			int start, int length, ColumnSort[] sorting)
			throws AccessDeniedException {
		List<SchedulerResource> list = new ArrayList<SchedulerResource>();
		SchedulerResource schedulerResource = null;
		JobKey jobKey = null;
		try {
			for (String group : scheduler.getJobGroupNames()) {
				Set<JobKey> scheduleKeys = scheduler.getJobKeys(GroupMatcher
						.jobGroupEquals(group));
				Set<String> keySet = new HashSet<String>(
						cachedResources.keySet());
				for (String key : keySet) {
					schedulerResource = cachedResources.get(key);
					jobKey = new JobKey(schedulerResource.getJobId(), group);
					if (scheduleKeys.contains(jobKey)) {
						List<? extends Trigger> triggersOfJob = scheduler
								.getTriggersOfJob(jobKey);
						if (triggersOfJob.size() > 0) {
							if (triggersOfJob.get(0).getJobDataMap()
									.containsKey("jobName")) {
								schedulerResource.setName(triggersOfJob.get(0)
										.getJobDataMap().getString("jobName"));
							} else {
								log.warn("Job has no name");
							}

							if (triggersOfJob.get(0).getPreviousFireTime() != null) {
								schedulerResource
										.setLastExecuted(HypersocketUtils
												.formatDate(triggersOfJob
														.get(0)
														.getPreviousFireTime(),
														"yyyy/MM/dd HH:mm"));
							}
							schedulerResource.setNextExecute(HypersocketUtils
									.formatDate(triggersOfJob.get(0)
											.getNextFireTime(),
											"yyyy/MM/dd HH:mm"));
							if (StringUtils.isBlank(search)) {
								list.add(schedulerResource);
							} else {
								String searchCritria = search.substring(0,
										search.length() - 1);
								if (schedulerResource.getName().toUpperCase().startsWith(searchCritria.toUpperCase())) {
									list.add(schedulerResource);
								}
							}
						}
					} else {
						cachedResources.remove(schedulerResource.getJobId());
					}
				}
			}
		} catch (SchedulerException e) {
			log.error("Error in getting Scheduler list ", e);
		}
		
		Collections.sort(list, new Comparator<SchedulerResource>() {

			@Override
			public int compare(SchedulerResource o1, SchedulerResource o2) {
				return o1.getName().toLowerCase().compareTo(o2.getName().toLowerCase());
			}
			
		});
		
		if(sorting.length > 0) {
			if(sorting[0].getSort()==Sort.DESC) {
				Collections.reverse(list);
			}
		}
		return getPage(list, start, length);
	}

	private List<SchedulerResource> getPage(List<SchedulerResource> resources,
			int start, int length) {
		if (resources.size() < (start + length)) {
			return resources.subList(start, resources.size());
		} else {
			return resources.subList(start, (start + length));
		}
	}

	@Override
	protected void fireResourceCreationEvent(SchedulerResource resource) {

	}

	@Override
	protected void fireResourceCreationEvent(SchedulerResource resource,
			Throwable t) {

	}

	@Override
	protected void fireResourceUpdateEvent(SchedulerResource resource) {

	}

	@Override
	protected void fireResourceUpdateEvent(SchedulerResource resource,
			Throwable t) {

	}

	@Override
	protected void fireResourceDeletionEvent(SchedulerResource resource) {

	}

	@Override
	protected void fireResourceDeletionEvent(SchedulerResource resource,
			Throwable t) {

	}

	@Override
	protected Class<SchedulerResource> getResourceClass() {
		return SchedulerResource.class;
	}

	@Override
	public long getResourceCount(Realm realm, String searchColumn, String search)
			throws AccessDeniedException {
		return cachedResources.size();
	}

}
