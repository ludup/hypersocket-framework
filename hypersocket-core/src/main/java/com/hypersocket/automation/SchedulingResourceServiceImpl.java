package com.hypersocket.automation;

import java.util.Date;
import java.util.UUID;

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
import com.hypersocket.scheduler.JobData;
import com.hypersocket.scheduler.NotScheduledException;
import com.hypersocket.utils.HypersocketUtils;

@Service
public class SchedulingResourceServiceImpl implements SchedulingResourceService {

	static Logger log = LoggerFactory.getLogger(SchedulingResourceServiceImpl.class);
	
	@Autowired
	private ClusteredSchedulerService schedulerService;

	@Autowired
	private ConfigurationService configurationService; 
	
	@Override
	public <T extends RealmResource> void unschedule(T resource) throws SchedulerException {

		String oldScheduleId = resource.getId().toString();
		try {
			if (schedulerService.jobExists(oldScheduleId)) {
				schedulerService.cancelNow(oldScheduleId);
			}
		} catch (SchedulerException e1) {
			log.error("Cannot unschedule old style job reference {}", oldScheduleId);
		}
		
		String newScheduleId = String.format("%s-%d", resource.getName().replaceAll("\\s",""), resource.getId());
		
		if (schedulerService.jobExists(newScheduleId)) {
			schedulerService.cancelNow(newScheduleId);
		}
	}
	
	protected Date calculateDateTime(Realm realm, Date from, String time) {
		String timezone = configurationService.getValue(realm, "realm.defaultTimezone");
		return HypersocketUtils.calculateDateTime(timezone, from, time);
	}
	
	
	@Override
	public <T extends RealmResource> void schedule(T resource, Date startDate, String startTime, Date endDate, 
			String endTime, AutomationRepeatType repeatType,
			int repeatValue, Class<? extends Job> clz) {

		Date start = calculateDateTime(resource.getRealm(), startDate, startTime);
		Date end = calculateDateTime(resource.getRealm(), endDate, endTime);

		long interval = 0;
		int repeat = -1;

		if (repeatValue > 0) {

			switch (repeatType) {
			case DAYS:
				interval = (long)repeatValue * (60000l * 60l * 24l);
				break;
			case HOURS:
				interval = (long)repeatValue * (60000l * 60l);
				break;
			case MINUTES:
				interval = (long)repeatValue * 60000l;
				break;
			case SECONDS:
				interval = (long)repeatValue * 1000l;
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
					start = new Date(start.getTime() + interval);
				}
			}
		}
		
		if(start!=null && start.before(now)) {
			if(log.isInfoEnabled()) {
				log.info("Not scheduling " + resource.getName() + " because its schedule is in the past.");
			}
			return;
		}

		String oldScheduleId = resource.getId().toString();
		try {
			if (schedulerService.jobExists(oldScheduleId)) {
				schedulerService.cancelNow(oldScheduleId);
			}
		} catch (SchedulerException e1) {
			log.error("Cannot remove old style job reference {}", oldScheduleId);
		}
		
		String newScheduleId = String.format("%s-%d", resource.getName().replaceAll("\\s",""), resource.getId());
		
		if(start==null && end==null) {
			if(repeatType==AutomationRepeatType.NEVER) {
				log.info("Not scheduling " + resource.getName() + " because it is a non-repeating job with no start or end date/time.");
				try {
					if (schedulerService.jobExists(newScheduleId)) {
						schedulerService.cancelNow(newScheduleId);
					}
				}
				catch(Exception e) {
					log.error("Failed to cancel existing job.", e);
				}
				return;
			}
		}
		
		var data = JobData.ofResource(resource);

		try {

			if (schedulerService.jobExists(newScheduleId)) {

				try {
					if (start == null) {
						schedulerService.rescheduleNow(newScheduleId, interval, repeat, end);
					} else {
						schedulerService.rescheduleAt(newScheduleId, start, interval, repeat, end);
					}
					return;
				} catch (NotScheduledException e) {
					if (log.isInfoEnabled()) {
						log.info("Attempted to reschedule job but it was not scheduled.");
					}
				}

			}

			if (start == null || start.before(new Date())) {
				schedulerService.scheduleNow(clz, newScheduleId, data, interval, repeat, end);
			} else {
				schedulerService.scheduleAt(clz, newScheduleId, data, start, interval, repeat, end);
			}

		} catch (SchedulerException e) {
			log.error("Failed to schedule automation task " + resource.getName(), e);
		}
	}
	
	@Override
	public <T extends RealmResource> void scheduleNow(T resource, Class<? extends Job> clz) {

		var data = JobData.ofResource(resource);

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
