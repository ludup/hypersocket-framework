package com.hypersocket.reconcile;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.Resource;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.scheduler.PermissionsAwareJob;
import com.hypersocket.scheduler.PermissionsAwareJobNonTransactional;

public abstract class AbstractReconcileNonTransactionalJob<T extends Resource> extends PermissionsAwareJobNonTransactional {

	static Logger log = LoggerFactory.getLogger(AbstractReconcileNonTransactionalJob.class);
	
	protected abstract AbstractReconcileService<T> getReconcileService();
	
	protected abstract T getResource(Long id) throws ResourceNotFoundException, AccessDeniedException;
	
	protected abstract void doReconcile(T resource) throws Exception;
	
	protected abstract void fireReconcileStartedEvent(T resource);
	
	protected abstract void fireReconcileCompletedEvent(T resource);
	
	protected abstract void fireReconcileFailedEvent(T resource, Throwable t);
	
	protected T resource; 

	@Override
	protected void executeJob(JobExecutionContext context) throws JobExecutionException {
		
		try {
			
			Long resourceId = (Long) context.getTrigger().getJobDataMap().get("resourceId");
			
			resource = getResource(resourceId);
			
			if (log.isInfoEnabled()) {
				log.info("Starting reconcile for resource " + resource.getName());
			}

			getReconcileService().lockResource(resource);
			
			fireReconcileStartedEvent(resource);
			
			doReconcile(resource);
		
			fireReconcileCompletedEvent(resource);
			
		} catch(Throwable t) {
			log.error("Resource reconcile failed", t);
			fireReconcileFailedEvent(resource, t);
			throw new IllegalStateException(t);
		} finally {
			getReconcileService().unlockResource(resource);
		}
		
		if (log.isInfoEnabled()) {
			log.info("Finished reconcile for resource " + resource.getName());
		}
	}
}
