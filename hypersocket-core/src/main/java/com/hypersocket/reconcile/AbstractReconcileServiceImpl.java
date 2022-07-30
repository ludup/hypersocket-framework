/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.reconcile;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import com.hypersocket.context.AuthenticatedContext;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.events.ResourceEvent;
import com.hypersocket.reconcile.events.ReconcileCompleteEvent;
import com.hypersocket.reconcile.events.ReconcileEvent;
import com.hypersocket.reconcile.events.ReconcileStartedEvent;
import com.hypersocket.resource.Resource;
import com.hypersocket.scheduler.ClusteredSchedulerService;
import com.hypersocket.scheduler.NotScheduledException;
import com.hypersocket.utils.HypersocketUtils;

@Service
public abstract class AbstractReconcileServiceImpl<T extends Resource> implements
		AbstractReconcileService<T>, ApplicationListener<SystemEvent> {

	static Logger log = LoggerFactory
			.getLogger(AbstractReconcileServiceImpl.class);
	
	public static final String RESOURCE_BUNDLE = "AbstractReconcileService";

	@Autowired
	private ClusteredSchedulerService schedulerService;

	@Autowired
	private I18NService i18nService;
	
	private Set<T> reconcilingResources = new HashSet<T>();
	
	public AbstractReconcileServiceImpl() {

	}
	
	@PostConstruct
	private void postConstruct() {
		i18nService.registerBundle(RESOURCE_BUNDLE);
	}

	protected abstract boolean isTriggerEvent(SystemEvent event);
	
	protected abstract Class<? extends Job> getReconcileJobClass();
	
	protected abstract int getReconcileSuccessInterval(T resource);
	
	protected abstract int getReconcileFailureInterval(T resource);
	
	protected abstract Collection<T> getReconcilingResources();
	
	protected abstract ResourceTemplateRepository getRepository();
	
	protected abstract Class<? extends ResourceEvent> getResourceCreatedEventClass();
	
	protected abstract Class<? extends ResourceEvent> getResourceUpdatedEventClass();
	
	protected abstract Class<? extends ResourceEvent> getResourceDeletedEventClass();
	
	protected abstract T getResourceFromEvent(SystemEvent event);
	
	protected abstract boolean isReconciledResource(T resource);
	
	@Override
	@AuthenticatedContext(system = true)
	public synchronized void onApplicationEvent(final SystemEvent event) {

		if (!isTriggerEvent(event)
				&& !(event instanceof ReconcileEvent)
				&& !event.getResourceKey().equals("event.serverStarted")) {
			return;
		}
		
	
		if (event.getResourceKey()
				.equals("event.serverStarted")) {
			
			scheduleReconciles();
			
		} else if (getResourceCreatedEventClass().isAssignableFrom(event.getClass())) {
			
			if(event.isSuccess()) {
				T resource = getResourceFromEvent(event);
				if(isReconciledResource(resource)) {
					scheduleReconcile(resource, false, true);
				}
			}

		} else if(getResourceUpdatedEventClass().isAssignableFrom(event.getClass())) {
			
			if(event.isSuccess()) {
				T resource = getResourceFromEvent(event);
				if(isReconciledResource(resource)) {
					rescheduleReconcile(resource);
				}
			}
		} else if(getResourceDeletedEventClass().isAssignableFrom(event.getClass())) {
			
			if(event.isSuccess()) {
				T resource = getResourceFromEvent(event);
				if(isReconciledResource(resource)) {
					unscheduleReconcile(resource);
				}
			}
			
		} else if (event instanceof ReconcileCompleteEvent) {
			T resource = getResourceFromEvent(event);
			
			/**
			 * If resource was not previously up to date then reschedule because
			 * we have different reconcile schedules for success and failure.
			 */
			boolean upToDate = getRepository().getBooleanValue(resource, "reconcile.upToDate");
			if(!upToDate && event.isSuccess()) {
				rescheduleReconcile(resource, true);
				getRepository().setValue(resource, "reconcile.upToDate", true);
			}else if(upToDate && !event.isSuccess()) {
				rescheduleReconcile(resource, false);
				getRepository().setValue(resource, "reconcile.upToDate", false);
			}
			
			
		}  else if (event instanceof ReconcileStartedEvent) {
			
			if(!event.isSuccess()) {
				T resource = getResourceFromEvent(event);
				if(isReconciledResource(resource)) {
					/**
					 * If the realm was previously up to date then reschedule reconcile because
					 * we have different reconcile schedules for success and failure
					 */
					boolean upToDate = getRepository().getBooleanValue(resource, "reconcile.upToDate");
					if(upToDate) {
						rescheduleReconcile(resource, false);
						getRepository().setValue(resource, "reconcile.upToDate", false);
					}
				}
			}
		
		}

	}
	
	private void scheduleReconciles() {
		for(T resource : getReconcilingResources()) {
			
			if(isReconciledResource(resource)) {
				boolean upToDate = getRepository().getBooleanValue(resource, "reconcile.isUpToDate");
				
				if(upToDate) {
					String nextDue = getRepository().getValue(resource, "reconcile.nextReconcileDue");
					try {
						if(StringUtils.isNotBlank(nextDue)) {
							Date nextDueDate = HypersocketUtils.parseDate(nextDue, "EEE, d MMM yyyy HH:mm:ss");
							if(nextDueDate.after(new Date())) {
								scheduleReconcileAt(resource, nextDueDate);
								return;
							}
						} 
					} catch (ParseException e) {
						log.error("Failed to parse date " + nextDue, e);
					}
				}
				
				/**
				 * If we reached here we need to schedule the reconcile now.				
				 */
				scheduleReconcile(resource, upToDate, false);
			}
			
		}
	}
	
	private void scheduleReconcileAt(T resource, Date startDate) {
		
		try {
			
			JobDataMap data = new JobDataMap();
			data.put("resourceId", resource.getId());
			data.put("jobName", "reconcileResourceJob");
			
			if(schedulerService.jobDoesNotExists(resource.getId().toString())) {
				schedulerService.scheduleAt(
							getReconcileJobClass(), 
							resource.getId().toString(),
							data,
							startDate,
							getReconcileSuccessInterval(resource));
			} else {
				schedulerService.rescheduleAt(resource.getId().toString(), 
						startDate,
						getReconcileSuccessInterval(resource));
			}
			
			updateResourceSchedule(resource);
		} catch (SchedulerException | NotScheduledException e) {
			log.error("Failed to schedule reconcile for resource " + resource.getName(),
					e);
		}
		
	}

	public boolean reconcileNow(T resource) {
		

		JobDataMap data = new JobDataMap();
		data.put("resourceId", resource.getId());
		data.put("jobName", "reconcileResourceJob");
		
		try {
			schedulerService.scheduleNow(getReconcileJobClass(), resource.getId().toString(), data);
			return true;
		} catch (SchedulerException e) {
			log.error("Failed to start immediate reconcile for resource " + resource.getName(), e);
			return false;
		}
	}
	
	private void scheduleReconcile(T resource, boolean upToDate, boolean initial) {
		try {

			JobDataMap data = new JobDataMap();
			data.put("resourceId", resource.getId());
			data.put("jobName", "reconcileResourceJob");
			data.put("initial", Boolean.valueOf(initial));
			
			if(schedulerService.jobDoesNotExists(resource.getId().toString())) {

					schedulerService.scheduleIn(
							getReconcileJobClass(), 
							resource.getId().toString(),
							data,
							5000,
							(upToDate ? 
							getReconcileSuccessInterval(resource)
							: getReconcileFailureInterval(resource)));
			
			}
			
			updateResourceSchedule(resource);
		} catch (SchedulerException e) {
			log.error("Failed to schedule reconcile for resource " + resource.getName(),
					e);
		}
	}
	
	private void rescheduleReconcile(T resource) {
		unscheduleReconcile(resource);
		scheduleReconcile(resource, 
				getRepository().getBooleanValue(resource, "reconcile.upToDate"), false);
	}
	
	@Override
	public void updateResourceSchedule(T resource) throws SchedulerException {

		try {
			Date previousReconcile = schedulerService.getPreviousSchedule(resource.getId().toString());
			
			getRepository().setValue(resource, "reconcile.nextReconcileDue", 
					HypersocketUtils.formatDate(
							schedulerService.getNextSchedule(resource.getId().toString()), 
								"EEE, d MMM yyyy HH:mm:ss"));
			
			getRepository().setValue(resource, "reconcile.lastReconcilePerformed", 
					HypersocketUtils.formatDate(
							previousReconcile,
							"EEE, d MMM yyyy HH:mm:ss"));
		} catch (NotScheduledException e) {
			scheduleReconcile(resource, false, false);
		}
		

	}
	
	private void rescheduleReconcile(final T resource, final boolean success) {

		if(log.isInfoEnabled()) {
			log.info(String.format("Rescheduling reconcile for %s in %d minutes", 
					resource.getName(),
					(success ? getReconcileSuccessInterval(resource) : getReconcileFailureInterval(resource))));
		}
		
		try {
			schedulerService.rescheduleIn(
							resource.getId().toString(),
							(success ? getReconcileSuccessInterval(resource) : getReconcileFailureInterval(resource)));
			
			updateResourceSchedule(resource);
			
		} catch (SchedulerException e) {
			log.error(String.format("Failed to reschedule a reconcile for realm %s adding back to unscheduled resource queue", resource.getName(), e));
		} catch (NotScheduledException e) {
			scheduleReconcile(resource, success, false);
		}		
	}
	
	private void unscheduleReconcile(T resource) {

		try {
			schedulerService.cancelNow(resource.getId().toString());
			
			getRepository().setValue(resource, "reconcile.nextReconcileDue", 
					I18N.getResource(Locale.getDefault(), RESOURCE_BUNDLE, "reconcile.notScheduled"));

		} catch (SchedulerException e) {
			log.error("Failed to cancel schedule for realm " + resource.getName(), e);
		}
	}

	@Override
	public synchronized void unlockResource(T resource) {
		reconcilingResources.remove(resource);		
	}

	@Override
	public synchronized void lockResource(T resource) {
		reconcilingResources.add(resource);
	}
	
	@Override
	public synchronized boolean isLocked(T resource) {
		return reconcilingResources.contains(resource);
	}
	
}
