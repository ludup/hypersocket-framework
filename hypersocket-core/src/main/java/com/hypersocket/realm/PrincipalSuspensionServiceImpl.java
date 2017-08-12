package com.hypersocket.realm;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.scheduler.ClusteredSchedulerService;
import com.hypersocket.scheduler.PermissionsAwareJobData;
import com.hypersocket.session.SessionService;

@Service
public class PrincipalSuspensionServiceImpl implements PrincipalSuspensionService {

	static Logger log = LoggerFactory.getLogger(PrincipalSuspensionServiceImpl.class);
	
	@Autowired
	ClusteredSchedulerService schedulerService;
	
	@Autowired
	PrincipalSuspensionRepository repository;
	
	@Autowired
	SessionService sessionService; 
	
	@Override
	public PrincipalSuspension createPrincipalSuspension(Principal principal,
			Date startDate, Long duration, PrincipalSuspensionType type) throws ResourceException {

		String name = principal.getPrincipalName();

		Collection<PrincipalSuspension> principalSuspensions = repository.getSuspensions(principal, type);
		
		PrincipalSuspension principalSuspension = null;
		
		if(principalSuspensions.isEmpty()) {
			principalSuspension = new PrincipalSuspension();
			principalSuspension.setPrincipal(principal);
			principalSuspension.setName(name);
			principalSuspension.setRealm(principal.getRealm());
			
		} else {
			principalSuspension = principalSuspensions.iterator().next();
		}
		
		principalSuspension.setStartTime(startDate);
		principalSuspension.setDuration(duration);
		principalSuspension.setSuspensionType(type);
	
		String scheduleId = principal.getId().toString();
		
		try {
			if (schedulerService.jobExists(scheduleId)) {
				if (log.isInfoEnabled()) {
					log.info(String.format("%s with scheduleId %s is already suspended. Rescheduling to new parameters",scheduleId,name));
				}
	
				if (log.isInfoEnabled()) {
					log.info(String.format("Cancelling existing schedule for %s with scheduleId %s",name,scheduleId));
				}
				
				schedulerService.cancelNow(scheduleId);
	
			}
		} catch (Exception e) {
			log.error("Failed to cancel suspend schedule for " + name, e);
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

	private void scheduleResume(Principal principal, Date startDate, long duration) throws ResourceException {
		
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
				principal.getRealm(), "resumeUserJob");
		data.put("name", principal.getPrincipalName());

		String scheduleId = principal.getId().toString();
		
		
		try {
			schedulerService.scheduleAt(ResumeUserJob.class, scheduleId, data, c.getTime());
		} catch (SchedulerException e) {
			throw new ResourceCreationException(RealmService.RESOURCE_BUNDLE,
					"error.suspendUser.schedule", e.getMessage());
		}

	}

	@Override
	public PrincipalSuspension deletePrincipalSuspension(Principal principal, PrincipalSuspensionType type) {
		Collection<PrincipalSuspension> suspensions = repository
				.getSuspensions(principal, type);
		if(suspensions.isEmpty()) {
			return null;
		}
		PrincipalSuspension suspension = suspensions.iterator().next();
		repository.deletePrincipalSuspension(suspension);
		return suspension;

	}

	public void notifyResume(String scheduleId, String name, boolean onSchedule) {

		if (!onSchedule && scheduleId != null) {
			try {
				schedulerService.cancelNow(scheduleId);
			} catch (SchedulerException e) {
				log.error("Failed to cancel resume job for user " + name.toString(), e);
			}
		}

	}

	@Override
	public PrincipalSuspension getSuspension(Principal principal, PrincipalSuspensionType type) {
		Collection<PrincipalSuspension> suspensions = repository.getSuspensions(principal, type);
		if(suspensions.isEmpty()) {
			return null;
		}
		return suspensions.iterator().next();
	}
	
	@Override
	public Collection<PrincipalSuspension> getSuspensions(Principal principal) {
		Collection<PrincipalSuspension> suspensions = repository.getSuspensions(principal);
		if(suspensions.isEmpty()) {
			return null;
		}
		return suspensions;
	}

}
