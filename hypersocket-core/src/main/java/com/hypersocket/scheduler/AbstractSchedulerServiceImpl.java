package com.hypersocket.scheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
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
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.properties.AbstractPropertyTemplate;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.Sort;
import com.hypersocket.utils.HypersocketUtils;

public abstract class AbstractSchedulerServiceImpl extends AbstractAuthenticatedServiceImpl
		implements SchedulerService {

	static Logger log = LoggerFactory.getLogger(AbstractSchedulerServiceImpl.class);

	private Scheduler scheduler;

	@Autowired
	private I18NService i18nService;
	@Autowired
	private PermissionService permissionService;

	@PostConstruct
	private void postConstruct() throws SchedulerException {

		scheduler = configureScheduler();
		i18nService.registerBundle(RESOURCE_BUNDLE);
	}

	protected abstract Scheduler configureScheduler() throws SchedulerException;

	@Override
	public void scheduleNow(Class<? extends Job> clz, String scheduleId, JobDataMap data) throws SchedulerException {
		scheduleNow(clz, scheduleId, data, 0, 0);
	}

	@Override
	public void scheduleNow(Class<? extends Job> clz, String scheduleId, JobDataMap data, long interval, int repeat)
			throws SchedulerException {
		schedule(clz, scheduleId, data, null, interval, repeat, null);
	}

	@Override
	public void scheduleNow(Class<? extends Job> clz, String scheduleId, JobDataMap data, long interval)
			throws SchedulerException {
		scheduleNow(clz, scheduleId, data, interval, SimpleTrigger.REPEAT_INDEFINITELY);
	}

	@Override
	public void scheduleNow(Class<? extends Job> clz, String scheduleId, JobDataMap data, long interval, int repeat,
			Date ends) throws SchedulerException {
		schedule(clz, scheduleId, data, null, interval, repeat, null);
	}

	@Override
	public void scheduleAt(Class<? extends Job> clz, String scheduleId, JobDataMap data, Date start)
			throws SchedulerException {
		schedule(clz, scheduleId, data, start, 0, 0, null);
	}

	@Override
	public void scheduleAt(Class<? extends Job> clz, String scheduleId, JobDataMap data, Date start, long interval)
			throws SchedulerException {
		schedule(clz, scheduleId, data, start, interval, SimpleTrigger.REPEAT_INDEFINITELY, null);
	}

	@Override
	public void scheduleAt(Class<? extends Job> clz, String scheduleId, JobDataMap data, Date start, long interval,
			int repeat) throws SchedulerException {
		schedule(clz, scheduleId, data, start, interval, repeat, null);
	}

	@Override
	public void scheduleAt(Class<? extends Job> clz, String scheduleId, JobDataMap data, Date start, long interval,
			int repeat, Date ends) throws SchedulerException {
		schedule(clz, scheduleId, data, start, interval, repeat, ends);
	}

	@Override
	public void scheduleIn(Class<? extends Job> clz, String scheduleId, JobDataMap data, int millis)
			throws SchedulerException {
		scheduleIn(clz, scheduleId, data, millis, 0, SimpleTrigger.REPEAT_INDEFINITELY);
	}

	@Override
	public void scheduleIn(Class<? extends Job> clz, String scheduleId, JobDataMap data, int millis, long interval)
			throws SchedulerException {
		schedule(clz, scheduleId, data, DateBuilder.futureDate(millis, DateBuilder.IntervalUnit.MILLISECOND), interval,
				SimpleTrigger.REPEAT_INDEFINITELY, null);
	}

	@Override
	public void scheduleIn(Class<? extends Job> clz, String scheduleId, JobDataMap data, int millis, long interval,
			Date ends) throws SchedulerException {
		schedule(clz, scheduleId, data, DateBuilder.futureDate(millis, DateBuilder.IntervalUnit.MILLISECOND), interval,
				SimpleTrigger.REPEAT_INDEFINITELY, ends);
	}

	@Override
	public void scheduleIn(Class<? extends Job> clz, String scheduleId, JobDataMap data, int millis, long interval,
			int repeat) throws SchedulerException {
		scheduleAt(clz, scheduleId, data, DateBuilder.futureDate(millis, DateBuilder.IntervalUnit.MILLISECOND),
				interval, repeat);
	}

	@Override
	public void rescheduleIn(String scheduleId, int millis, long interval, int repeat)
			throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, DateBuilder.futureDate(millis, DateBuilder.IntervalUnit.MILLISECOND), interval, repeat,
				null);
	}

	@Override
	public void rescheduleIn(String scheduleId, int millis, long interval)
			throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, DateBuilder.futureDate(millis, DateBuilder.IntervalUnit.MILLISECOND), interval,
				SimpleTrigger.REPEAT_INDEFINITELY, null);
	}

	@Override
	public void rescheduleIn(String scheduleId, int millis) throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, DateBuilder.futureDate(millis, DateBuilder.IntervalUnit.MILLISECOND), 0, 0, null);
	}

	@Override
	public void rescheduleAt(String scheduleId, Date time, long interval, int repeat)
			throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, time, interval, repeat, null);
	}

	@Override
	public void rescheduleAt(String scheduleId, Date time, long interval, int repeat, Date end)
			throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, time, interval, repeat, end);
	}

	@Override
	public void rescheduleAt(String scheduleId, Date time, long interval)
			throws SchedulerException, NotScheduledException {
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
	public void rescheduleNow(String scheduleId, long interval) throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, null, interval, SimpleTrigger.REPEAT_INDEFINITELY, null);
	}

	@Override
	public void rescheduleNow(String scheduleId, long interval, int repeat)
			throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, null, interval, repeat, null);
	}

	@Override
	public void rescheduleNow(String scheduleId, long interval, int repeat, Date end)
			throws SchedulerException, NotScheduledException {
		reschedule(scheduleId, null, interval, repeat, end);
	}

	protected void schedule(Class<? extends Job> clz, String scheduleId, JobDataMap data, Date start, long interval,
			int repeat, Date end) throws SchedulerException {

		if (scheduleId == null) {
			throw new IllegalArgumentException("Schedule id cannot be null");
		}

		if (jobExists(scheduleId)) {
			if (log.isInfoEnabled()) {
				log.info(String.format("The job with identity %s already exists will not be scheduled again !!!!!!!.",
						scheduleId));
			}
		}

		if (log.isInfoEnabled()) {
			log.info("Scheduling job " + clz.getSimpleName() + " with id " + scheduleId + " to start "
					+ (start == null ? "now" : "at " + HypersocketUtils.formatDateTime(start)) + " with interval of "
					+ (interval / 60000) + " minutes and repeat "
					+ (repeat >= 0 ? (repeat / 60000) + " time(s)" : "indefinately")
					+ (end != null ? " until " + HypersocketUtils.formatDateTime(end) : ""));
		}
		JobDetail job = JobBuilder.newJob(clz).withIdentity(scheduleId).build();

		Trigger trigger = createTrigger(data, start, interval, repeat, end);

		/*
		 * BUG?: This properties map is not getting set anywhere? I presume it should be
		 */
		Map<String, String> properties = new HashMap<String, String>();
		properties.put("started", (start == null ? HypersocketUtils.formatDate(new Date(), "yyyy/MM/dd HH:mm")
				: HypersocketUtils.formatDate(start, "yyyy/MM/dd HH:mm")));
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

	protected Trigger createTrigger(JobDataMap data, Date start, long interval, int repeat, Date end) {

		TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();

		if (data != null) {
			triggerBuilder.usingJobData(data);
		}
		if (interval > 0) {
			SimpleScheduleBuilder schedule = SimpleScheduleBuilder.simpleSchedule();
			schedule.withIntervalInMilliseconds(interval).withRepeatCount(repeat);
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

	protected void reschedule(String id, Date start, long interval, int repeat, Date end)
			throws SchedulerException, NotScheduledException {

		if (log.isInfoEnabled()) {
			log.info("Rescheduling job with id " + id + " to start "
					+ (start == null ? "now" : "at " + HypersocketUtils.formatDateTime(start)) + " with interval of "
					+ interval + " and repeat " + (repeat >= 0 ? repeat : "indefinatley")
					+ (end != null ? " until " + HypersocketUtils.formatDateTime(end) : ""));
		}
		TriggerKey triggerKey = scheduler.getTriggersOfJob(new JobKey(id)).get(0).getKey();

		if (triggerKey != null) {

			Trigger oldTrigger = scheduler.getTrigger(triggerKey);
			Trigger trigger = createTrigger(oldTrigger.getJobDataMap(), start, interval, repeat, end);
			scheduler.rescheduleJob(triggerKey, trigger);
			/*
			 * BUG?: This properties map is not getting set anywhere? I presume it should be
			 */
			Map<String, String> properties = new HashMap<String, String>();
			properties.put("started", (start == null ? HypersocketUtils.formatDate(new Date(), "yyyy/MM/dd HH:mm")
					: HypersocketUtils.formatDate(start, "yyyy/MM/dd HH:mm")));
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
		if (triggersOfJob.isEmpty()) {
			throw new NotScheduledException();
		}
		Trigger trigger = triggersOfJob.get(0);
		return trigger.getNextFireTime();
	}

	@Override
	public Date getPreviousSchedule(String id) throws SchedulerException, NotScheduledException {
		List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(new JobKey(id));
		if (triggersOfJob.isEmpty()) {
			throw new NotScheduledException();
		}
		Trigger trigger = triggersOfJob.get(0);
		return trigger.getPreviousFireTime();
	}

	@Override
	public List<SchedulerResource> searchResources(Realm currentRealm, String searchColumn, String searchPattern,
			int start, int length, ColumnSort[] sorting) throws AccessDeniedException {
		List<SchedulerResource> l = new ArrayList<>();
		try {
			for (SchedulerResource r : getResources(currentRealm)) {
				if (r.matches(searchPattern, searchColumn))
					l.add(r);
			}
		} catch (SchedulerException se) {
			throw new IllegalStateException("Failed to search.", se);
		}
		final List<SchedulerResource> subList = l.subList(Math.min(l.size(), start),
				Math.min(l.size(), start + length));
		Collections.sort(subList, new Comparator<SchedulerResource>() {

			@SuppressWarnings("unchecked")
			@Override
			public int compare(SchedulerResource o1, SchedulerResource o2) {
				for (ColumnSort s : sorting) {
					int i = 0;
					Comparable<?> v1 = o1.getId();
					Comparable<?> v2 = o2.getId();
					if (s.getColumn() == SchedulerResourceColumns.NAME) {
						v1 = o1.getName() == null ? null : o1.getName().toLowerCase();
						v2 = o2.getName() == null ? null : o2.getName().toLowerCase();
					} else if (s.getColumn() == SchedulerResourceColumns.GROUP) {
						v1 = o1.getGroup();
						v2 = o2.getGroup();
					} else if (s.getColumn() == SchedulerResourceColumns.ID) {

						v1 = o1.getId() == null ? null : o1.getId().toLowerCase();
						v2 = o2.getId() == null ? null : o2.getId().toLowerCase();
					} else if (s.getColumn() == SchedulerResourceColumns.LASTFIRE) {
						v1 = o1.getLastFire();
						v2 = o2.getLastFire();
					} else if (s.getColumn() == SchedulerResourceColumns.NEXTFIRE) {
						v1 = o1.getNextFire();
						v2 = o2.getNextFire();
					} else if (s.getColumn() == SchedulerResourceColumns.STATUS) {
						v1 = o1.getStatus();
						v2 = o2.getStatus();
					}
					if (v1 == null && v2 != null)
						i = -1;
					else if (v2 == null && v1 != null)
						i = 1;
					else if (v2 != null && v1 != null) {
						i = (((Comparable<Object>) v1).compareTo((Comparable<Object>) v2));
					}
					if (i != 0) {
						return s.getSort() == Sort.ASC ? i * -1 : i;
					}
				}
				return 0;
			}
		});
		return subList;
	}

	@Override
	public Long getResourceCount(Realm currentRealm, String searchColumn, String searchPattern)
			throws AccessDeniedException {
		long v = 0;
		try {
			for (SchedulerResource r : getResources(currentRealm)) {
				if (r.matches(searchPattern, searchColumn))
					v++;
			}
		} catch (SchedulerException se) {
			throw new IllegalStateException("Failed to search.", se);
		}
		return v;
	}

	@Override
	public Collection<SchedulerResource> getResources(Realm currentRealm)
			throws SchedulerException, AccessDeniedException {
		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			assertPermission(SystemPermission.SYSTEM);
		}
		List<SchedulerResource> r = new ArrayList<>();
		for (String gn : scheduler.getJobGroupNames()) {
			for (JobKey k : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(gn))) {
				List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(k);
				if (triggersOfJob.isEmpty())
					r.add(new SchedulerResource(k));
				else
					r.add(buildSchedulerResource(triggersOfJob.get(0)));
			}
		}
		return r;
	}

	@Override
	public SchedulerResource getResourceById(String id)
			throws SchedulerException, NotScheduledException, AccessDeniedException {
		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			assertPermission(SystemPermission.SYSTEM);
		}
		List<? extends Trigger> triggersOfJob = scheduler.getTriggersOfJob(new JobKey(id));
		if (triggersOfJob.isEmpty()) {
			throw new NotScheduledException();
		}
		return buildSchedulerResource(triggersOfJob.get(0));
	}

	protected SchedulerResource buildSchedulerResource(Trigger trigger) throws SchedulerException {
		return new SchedulerResource(trigger, scheduler.getTriggerState(trigger.getKey()));
	}

	@Override
	public void deleteResource(SchedulerResource resource) throws SchedulerException, AccessDeniedException {
		if (!permissionService.hasAdministrativePermission(getCurrentPrincipal())) {
			assertPermission(SystemPermission.SYSTEM);
		}
		scheduler.deleteJob(resource.getJobKey());
	}

	@Override
	public List<SchedulerResource> getResourcesByIds(String[] ids) throws SchedulerException, AccessDeniedException {
		List<SchedulerResource> s = new ArrayList<>(ids.length);
		for (String i : ids)
			try {
				s.add(getResourceById(i));
			} catch (NotScheduledException e) {
			}
		return s;
	}

	@Override
	public void deleteResources(List<SchedulerResource> resources) throws SchedulerException, AccessDeniedException {
		for (SchedulerResource r : resources)
			deleteResource(r);
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate(SchedulerResource resource) {
		Collection<PropertyCategory> defs = getPropertyTemplate();
		PropertyCategory pc = defs.iterator().next();
		for (AbstractPropertyTemplate t : pc.getTemplates())
			((SchedulerPropertyTemplate) t).setResource(resource);
		return defs;
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate() {
		PropertyCategory pc = new PropertyCategory();
		pc.setBundle(RESOURCE_BUNDLE);
		pc.setCategoryKey("scheduler");
		pc.setWeight(100);

		SchedulerPropertyTemplate id = new SchedulerPropertyTemplate();
		id.setResourceKey("id");
		id.setWeight(100);
		id.getAttributes().put("inputType", "text");
		pc.getTemplates().add(id);

		SchedulerPropertyTemplate name = new SchedulerPropertyTemplate();
		name.setResourceKey("name");
		name.setWeight(200);
		name.getAttributes().put("inputType", "text");
		pc.getTemplates().add(name);

		SchedulerPropertyTemplate group = new SchedulerPropertyTemplate();
		group.setResourceKey("group");
		group.setWeight(300);
		group.getAttributes().put("inputType", "text");
		pc.getTemplates().add(group);

		SchedulerPropertyTemplate description = new SchedulerPropertyTemplate();
		description.setResourceKey("description");
		description.setWeight(300);
		description.getAttributes().put("inputType", "textarea");
		pc.getTemplates().add(description);

		SchedulerPropertyTemplate nextFire = new SchedulerPropertyTemplate();
		nextFire.setResourceKey("nextFire");
		nextFire.setWeight(400);
		nextFire.getAttributes().put("inputType", "time");
		pc.getTemplates().add(nextFire);

		SchedulerPropertyTemplate lastFire = new SchedulerPropertyTemplate();
		lastFire.setResourceKey("lastFire");
		lastFire.setWeight(500);
		lastFire.getAttributes().put("inputType", "time");
		pc.getTemplates().add(lastFire);

		return Arrays.asList(pc);
	}

	static class SchedulerPropertyTemplate extends AbstractPropertyTemplate {
		private SchedulerResource resource;

		@Override
		public String getValue() {
			if (resource != null) {
				if (getResourceKey().equals("id"))
					return resource.getId();
				if (getResourceKey().equals("name"))
					return resource.getName();
				if (getResourceKey().equals("group"))
					return resource.getGroup();
				if (getResourceKey().equals("description"))
					return resource.getDescription();
				if (getResourceKey().equals("nextFire"))
					return resource.getNextFire() == null ? null : String.valueOf(resource.getNextFire().getTime());
				if (getResourceKey().equals("lastFire"))
					return resource.getLastFire() == null ? null : String.valueOf(resource.getLastFire().getTime());
			}
			return null;
		}

		public void setResource(SchedulerResource resource) {
			this.resource = resource;
		}

	}
}
