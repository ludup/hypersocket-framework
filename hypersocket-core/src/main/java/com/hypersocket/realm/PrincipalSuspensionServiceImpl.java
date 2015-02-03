package com.hypersocket.realm;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.stereotype.Service;

import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.scheduler.PermissionsAwareJobData;
import com.hypersocket.scheduler.SchedulerService;

@Service
public class PrincipalSuspensionServiceImpl implements PrincipalSuspensionService, ApplicationListener<ContextStartedEvent> {

	static Logger log = LoggerFactory.getLogger(PrincipalSuspensionServiceImpl.class);
	
	@Autowired
	SchedulerService schedulerService;
	
	@Autowired
	PrincipalSuspensionRepository repository;

	Map<String, String> suspendedUserResumeSchedules = new HashMap<String, String>();
	
	
	
	@Override
	public PrincipalSuspension createPrincipalSuspension(Principal principal,
			Date startDate, Long duration) throws ResourceNotFoundException,
			ResourceCreationException {

		String name = principal.getPrincipalName();

		PrincipalSuspension principalSuspension;
		
		principalSuspension = repository.getSuspension(principal);
		
		if(principalSuspension==null) {
			principalSuspension = new PrincipalSuspension();
			principalSuspension.setPrincipal(principal);
			principalSuspension.setName(name);
			principalSuspension.setRealm(principal.getRealm());

		}
		
		principalSuspension.setStartTime(startDate);
		principalSuspension.setDuration(duration);
		
		if (suspendedUserResumeSchedules.containsKey(name)) {
			if (log.isInfoEnabled()) {
				log.info(name
						+ " is already suspended. Rescheduling to new parameters");
			}

			String scheduleId = suspendedUserResumeSchedules.get(name);

			if (log.isInfoEnabled()) {
				log.info("Cancelling existing schedule for " + name);
			}

			try {
				schedulerService.cancelNow(scheduleId);
			} catch (Exception e) {
				log.error("Failed to cancel suspend schedule for " + name, e);
			}

		}

		repository.saveSuspension(principalSuspension);

		if (duration > 0) {

			if (log.isInfoEnabled()) {
				log.info("Scheduling resume account for account " + name
						+ " in " + duration + " minutes");
			}

			scheduleResume(principal, startDate, duration);
			
		}

		return principalSuspension;
	}

	private void scheduleResume(Principal principal, Date startDate, long duration) throws ResourceCreationException {
		
		Calendar c = Calendar.getInstance();
		c.setTime(startDate);
		c.add(Calendar.MINUTE, (int) duration); 
		
		if(new Date().after(c.getTime())) {
			
			if(log.isInfoEnabled()) {
				log.info("Not scheduling resume because the suspension has already expired");
			}
			return;
		}
		PermissionsAwareJobData data = new PermissionsAwareJobData(
				principal.getRealm());
		data.put("name", principal.getPrincipalName());

		String scheduleId;
		
		
		try {
			scheduleId = schedulerService.scheduleAt(ResumeUserJob.class,
					data, c.getTime());
		} catch (SchedulerException e) {
			throw new ResourceCreationException(RealmService.RESOURCE_BUNDLE,
					"error.suspendUser.schedule", e.getMessage());
		}

		suspendedUserResumeSchedules.put(principal.getName(), scheduleId);
		
	}

	@Override
	public PrincipalSuspension deletePrincipalSuspension(Principal principal) {
		PrincipalSuspension suspension = repository
				.getSuspension(principal);

		repository.deletePrincipalSuspension(suspension);

		return suspension;
	}

	public void notifyResume(String name, boolean onSchedule) {

		String scheduleId = suspendedUserResumeSchedules.remove(name);

		if (!onSchedule && scheduleId != null) {
			try {
				schedulerService.cancelNow(scheduleId);
			} catch (SchedulerException e) {
				log.error(
						"Failed to cancel resume job for user "
								+ name.toString(), e);
			}
		}

	}

	@Override
	public PrincipalSuspension getSuspension(Principal principal) {
		return repository.getSuspension(principal);
	}

	@Override
	public void onApplicationEvent(ContextStartedEvent event) {
		
		for(PrincipalSuspension s : repository.getSuspensions()) {
			try {
				if (s.getDuration() > 0) {
					scheduleResume(s.getPrincipal(), s.getStartTime(), s.getDuration());
				}
			} catch (ResourceCreationException e) {
				log.error("Could not schedule resumption of user account " + s.getPrincipal().getName(), e);
			}
		}
	}
}
