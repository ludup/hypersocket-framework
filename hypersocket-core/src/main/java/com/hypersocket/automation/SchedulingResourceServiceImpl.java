package com.hypersocket.automation;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.quartz.Job;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.config.ConfigurationService;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.RealmResource;
import com.hypersocket.scheduler.ClusteredSchedulerService;
import com.hypersocket.scheduler.NotScheduledException;
import com.hypersocket.scheduler.PermissionsAwareJobData;
import com.hypersocket.utils.HypersocketUtils;

@Service
public class SchedulingResourceServiceImpl implements SchedulingResourceService {

	static Logger log = LoggerFactory.getLogger(SchedulingResourceServiceImpl.class);
	
	@Autowired
	ClusteredSchedulerService schedulerService;

	@Autowired
	ConfigurationService configurationService; 
	
	@Override
	public <T extends RealmResource> void unschedule(T resource) throws SchedulerException {

		if (schedulerService.jobExists(resource.getId().toString())) {
			schedulerService.cancelNow(resource.getId().toString());
		}
	}
	
	protected Date calculateDateTime(Realm realm, Date from, String time) {

		Calendar c = Calendar.getInstance();
		String timezone = configurationService.getValue(realm, "realm.defaultTimezone");
		if(StringUtils.isBlank(timezone)) {
			timezone = TimeZone.getDefault().getID();
		}
		
		c.setTime(HypersocketUtils.today());
		c.setTimeZone(TimeZone.getTimeZone(timezone));
		
		Date ret = null;

		if (from != null) {
			c.setTime(from);
			ret = c.getTime();
		}

		if (!StringUtils.isEmpty(time)) {
			int idx = time.indexOf(':');
			c.set(Calendar.HOUR_OF_DAY, Integer.parseInt(time.substring(0, idx)));
			c.set(Calendar.MINUTE, Integer.parseInt(time.substring(idx + 1)));
			ret = c.getTime();
		}

		return ret;
	}
	
	
	@Override
	public <T extends RealmResource> void schedule(T resource, Date startDate, String startTime, Date endDate, 
			String endTime, AutomationRepeatType repeatType,
			int repeatValue, Class<? extends Job> clz) {

		Date start = calculateDateTime(resource.getRealm(), startDate, startTime);
		Date end = calculateDateTime(resource.getRealm(), endDate, endTime);

		int interval = 0;
		int repeat = -1;

		if (repeatValue > 0) {

			switch (repeatType) {
			case DAYS:
				interval = repeatValue * (60000 * 60 * 24);
				break;
			case HOURS:
				interval = repeatValue * (60000 * 60);
				break;
			case MINUTES:
				interval = repeatValue * 60000;
				break;
			case SECONDS:
				interval = repeatValue * 1000;
				break;
			case NEVER:
			default:
				interval = 0;
				repeat = 0;
				break;
			}
		}

		Date now = new Date();
		if(start!=null && start.before(now)) {
			if(end!=null && end.before(now)) {
				// Start tomorrow, end tomorrow
				if(startDate==null) {
					start = DateUtils.addDays(start, 1);
				}
				if(endDate==null) {
					end = DateUtils.addDays(end, 1);
				}
			} else if(interval == 0) {
				// Start tomorrow?
				if(startDate==null) {
					start = DateUtils.addDays(start, 1);
				}
			} else if(interval > 0) {
				while(start.before(now)) {
					start = DateUtils.addMilliseconds(start, interval);
				}
			}
		}
		
		if(start!=null && start.before(now)) {
			if(log.isInfoEnabled()) {
				log.info("Not scheduling " + resource.getName() + " because its schedule is in the past.");
			}
			return;
		}

		String scheduleId = resource.getId().toString();
		
		if(start==null && end==null) {
			if(repeatType==AutomationRepeatType.NEVER) {
				log.info("Not scheduling " + resource.getName() + " because it is a non-repeating job with no start or end date/time.");
				try {
					if (schedulerService.jobExists(scheduleId)) {
						schedulerService.cancelNow(scheduleId);
					}
				}
				catch(Exception e) {
					log.error("Failed to cancel existing job.", e);
				}
				return;
			}
		}
		
		PermissionsAwareJobData data = new PermissionsAwareJobData(resource.getRealm(), resource.getName());
		data.put("resourceId", resource.getId());

		try {

			if (schedulerService.jobExists(scheduleId)) {

				try {
					if (start == null) {
						schedulerService.rescheduleNow(scheduleId, interval, repeat, end);
					} else {
						schedulerService.rescheduleAt(scheduleId, start, interval, repeat, end);
					}
					return;
				} catch (NotScheduledException e) {
					if (log.isInfoEnabled()) {
						log.info("Attempted to reschedule job but it was not scheduled.");
					}
				}

			}

			if (start == null || start.before(new Date())) {
				schedulerService.scheduleNow(clz, scheduleId, data, interval, repeat, end);
			} else {
				schedulerService.scheduleAt(clz, scheduleId, data, start, interval, repeat, end);
			}

		} catch (SchedulerException e) {
			log.error("Failed to schedule automation task " + resource.getName(), e);
		}
	}
	
	@Override
	public <T extends RealmResource> void scheduleNow(T resource, Class<? extends Job> clz) {

		PermissionsAwareJobData data = new PermissionsAwareJobData(resource.getRealm(), resource.getName());
		data.put("resourceId", resource.getId());

		try {
			if(log.isInfoEnabled()) {
				log.info(String.format("Scheduling %s", clz.getName()));
			}
			schedulerService.scheduleNow(clz, UUID.randomUUID().toString(), data);
		} catch (SchedulerException e) {
			log.error("Failed to schedule task " + resource.getName(), e);
		}
	}
}