package com.hypersocket.realm;

import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.events.PrincipalResumedEvent;
import com.hypersocket.realm.events.PrincipalSuspendedEvent;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.scheduler.ClusteredSchedulerService;
import com.hypersocket.scheduler.PermissionsAwareJobData;
import com.hypersocket.session.SessionService;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.tables.DefaultTableFilter;
import com.hypersocket.transactions.TransactionService;

@Service
public class PrincipalSuspensionServiceImpl implements PrincipalSuspensionService {

	static Logger log = LoggerFactory.getLogger(PrincipalSuspensionServiceImpl.class);
	
	public static final String RESOURCE_BUNDLE = "PrincipalSuspensionService";
	
	@Autowired
	private ClusteredSchedulerService schedulerService;
	
	@Autowired
	private PrincipalSuspensionRepository repository;
	
	@Autowired
	private PrincipalRepository principalRepository;
	
	@Autowired
	private TransactionService transactionService;
	
	@Autowired
	private RealmService realmService;
	
	@Autowired
	private I18NService i18nService; 
	
	@Autowired
	private EventService eventService; 
	
	@Autowired
	private SessionService sessionService; 
	
	@PostConstruct
	private void postConstruct() {
		
		i18nService.registerBundle(RESOURCE_BUNDLE);
		
		realmService.registerPrincipalFilter(new PrincipalSuspendedFilter());
		realmService.registerPrincipalFilter(new PrincipalEnabledFilter());

		eventService.registerEvent(PrincipalSuspendedEvent.class, RealmServiceImpl.RESOURCE_BUNDLE);
		eventService.registerEvent(PrincipalResumedEvent.class, RealmServiceImpl.RESOURCE_BUNDLE);
	}

	
	@Override
	public PrincipalSuspension createPrincipalSuspension(Principal principal, String name, Realm realm,
			Date startDate, Long duration, PrincipalSuspensionType type) throws ResourceException {

		try {
			return transactionService.doInTransaction(new TransactionCallback<PrincipalSuspension>() {

				@Override
				public PrincipalSuspension doInTransaction(TransactionStatus status) {
					Collection<PrincipalSuspension> principalSuspensions = repository.getSuspensions(name, realm, type);
					
					PrincipalSuspension principalSuspension = null;
					
					if(principalSuspensions.isEmpty()) {
						principalSuspension = new PrincipalSuspension();
						principalSuspension.setPrincipal(principal);
						principalSuspension.setName(name);
						principalSuspension.setRealm(realm);
						
					} else {
						principalSuspension = principalSuspensions.iterator().next();
					}
					
					principalSuspension.setStartTime(startDate);
					principalSuspension.setDuration(duration);
					principalSuspension.setSuspensionType(type);
				
					clearExistingJob(name, realm);

					repository.saveSuspension(principalSuspension);
					
					Date now = new Date();
					
					if (startDate.after(now)) {
						actOnSuspensionInFuture(name, realm, startDate, duration);
					} else {
						actOnSuspensionNow(principal, name, realm, startDate, duration);
					}

					return principalSuspension;
				}
			
			});
		} catch (AccessDeniedException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		
	}

	@Override
	public PrincipalSuspension deletePrincipalSuspension(Principal principal, PrincipalSuspensionType type) {
		try {
			return transactionService.doInTransaction(new TransactionCallback<PrincipalSuspension>() {

				@Override
				public PrincipalSuspension doInTransaction(TransactionStatus status) {
					Collection<PrincipalSuspension> suspensions = repository
							.getSuspensions(principal.getPrincipalName(), principal.getRealm(), type);
					if(suspensions.isEmpty()) {
						return null;
					}
					PrincipalSuspension suspension = suspensions.iterator().next();
					repository.deletePrincipalSuspension(suspension);
					
					updateSuspension(principal, false);
					
					return suspension;
				}
			});
		} catch (ResourceException | AccessDeniedException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
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
	public PrincipalSuspension getSuspension(String username, Realm realm, PrincipalSuspensionType type) {
		Collection<PrincipalSuspension> suspensions = repository.getSuspensions(username, realm, type);
		if(suspensions.isEmpty()) {
			return null;
		}
		return suspensions.iterator().next();
	}
	
	@Override
	public Collection<PrincipalSuspension> getSuspensions(String username, Realm realm) {
		Collection<PrincipalSuspension> suspensions = repository.getSuspensions(username, realm);
		if(suspensions.isEmpty()) {
			return null;
		}
		return suspensions;
	}

	private void updateSuspension(Principal principal, Boolean suspension) {
		try {
			if(!Objects.equals(suspension, principal.isSuspended())) {
				principal.setSuspended(suspension);
				principalRepository.saveResource(principal);
				if(suspension)
					eventService.publishEvent(
							new PrincipalSuspendedEvent(this, sessionService.getSystemSession(), sessionService.getSystemSession().getCurrentPrincipal(), principal));
				else
					eventService.publishEvent(
							new PrincipalResumedEvent(this, sessionService.getSystemSession(), sessionService.getSystemSession().getCurrentPrincipal(), principal));
			}
		} catch (ResourceException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	private void scheduleResume(String username, Realm realm, Date startDate, long duration) {
		
		try {
			var c = Calendar.getInstance();
			c.setTime(startDate);
			c.add(Calendar.MINUTE, (int) duration); 
			
			if(new Date().after(c.getTime())) {
				if(log.isInfoEnabled()) {
					log.info("Not scheduling resume because the suspension has already expired");
				}
				return;
			}
			var data = new PermissionsAwareJobData(realm, "resumeUserJob", username);
			data.put("name", username);

			var scheduleId = username + "/" + realm.getId();
			
			try {
				schedulerService.scheduleAt(ResumeUserJob.class, scheduleId, data, c.getTime());
			} catch (SchedulerException e) {
				throw new ResourceCreationException(RealmService.RESOURCE_BUNDLE,
						"error.suspendUser.schedule", e.getMessage());
			}
		} catch (ResourceCreationException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}

	}
	
	private void scheduleSuspend(String username, Realm realm, Date startDate, long duration) {
		
		try {
			var c = Calendar.getInstance();
			c.setTime(startDate);
			
			if(new Date().after(c.getTime())) {
				if(log.isInfoEnabled()) {
					log.info("Not scheduling suspension because the suspension has already expired");
				}
				return;
			}
			var data = new PermissionsAwareJobData(realm, "suspendUserJob", username);
			data.put("name", username);
			data.put("duration", duration);

			String scheduleId = username + "/" + realm.getId();
			
			try {
				schedulerService.scheduleAt(SuspendUserJob.class, scheduleId, data, c.getTime());
			} catch (SchedulerException e) {
				throw new ResourceCreationException(RealmService.RESOURCE_BUNDLE,
						"error.suspendUser.schedule", e.getMessage());
			}
		} catch (ResourceCreationException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}

	}
	
	void actOnSuspensionNow(Principal principal, String name, Realm realm, Date startDate, Long duration) {
		updateSuspension(principal, true);

		if (duration > 0) {

			if (log.isInfoEnabled()) {
				log.info("Scheduling resume account for account " + name
						+ " in " + duration + " minutes");
			}

			scheduleResume(name, realm, startDate, duration);
			
		}
	}

	void actOnSuspensionInFuture(String name, Realm realm, Date startDate, Long duration) {
		if (log.isInfoEnabled()) {
			log.info("Scheduling suspend account for account " + name
					+ " at " + startDate);
		}
		
		scheduleSuspend(name, realm, startDate, duration);
	}

	void clearExistingJob(String name, Realm realm) {
		String scheduleId = name + "/" + realm.getId();
		
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
	}

	class PrincipalSuspendedFilter extends DefaultTableFilter {

		@Override
		public String getResourceKey() {
			return "filter.principal.suspended";
		}

		@Override
		public List<?> searchResources(Realm realm, String searchColumn, String searchPattern, int start, int length,
				ColumnSort[] sorting) {
			return principalRepository.searchSuspendedState(Principal.class, realm, PrincipalType.USER, 
					searchColumn, searchPattern, sorting, start, length, true);
		}

		@Override
		public Long searchResourcesCount(Realm realm, String searchColumn, String searchPattern) {
			return principalRepository.getSuspendedStateCount(Principal.class, realm, PrincipalType.USER, searchColumn, searchPattern, true);
		}

		@Override
		public List<?> searchPersonalResources(Principal principal, String searchColumn, String searchPattern,
				int start, int length, ColumnSort[] sorting) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Long searchPersonalResourcesCount(Principal principal, String searchColumn, String searchPattern) {
			throw new UnsupportedOperationException();
		}

	}
	
	class PrincipalEnabledFilter extends DefaultTableFilter {

		@Override
		public String getResourceKey() {
			return "filter.principal.enabled";
		}

		@Override
		public List<?> searchResources(Realm realm, String searchColumn, String searchPattern, int start, int length,
				ColumnSort[] sorting) {
			return principalRepository.searchSuspendedState(Principal.class, realm, PrincipalType.USER, 
					searchColumn, searchPattern, sorting, start, length, false);
		}

		@Override
		public Long searchResourcesCount(Realm realm, String searchColumn, String searchPattern) {
			return principalRepository.getSuspendedStateCount(Principal.class, realm, PrincipalType.USER, searchColumn, searchPattern, false);
		}

		@Override
		public List<?> searchPersonalResources(Principal principal, String searchColumn, String searchPattern,
				int start, int length, ColumnSort[] sorting) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Long searchPersonalResourcesCount(Principal principal, String searchColumn, String searchPattern) {
			throw new UnsupportedOperationException();
		}

	}
}
