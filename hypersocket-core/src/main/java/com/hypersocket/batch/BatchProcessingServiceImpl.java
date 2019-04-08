package com.hypersocket.batch;

import java.util.Collection;

import javax.annotation.PostConstruct;
import javax.cache.Cache;

import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.cache.CacheService;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.RealmResource;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.scheduler.ClusteredSchedulerService;
import com.hypersocket.scheduler.PermissionsAwareJobData;

public abstract class BatchProcessingServiceImpl<T extends RealmResource> implements BatchProcessingService<T> {

	static Logger log = LoggerFactory.getLogger(BatchProcessingServiceImpl.class);
	
	protected abstract BatchProcessingItemRepository<T> getRepository();
	
	protected abstract int getBatchInterval();
	
	protected abstract boolean process(T item);
	
	protected abstract String getResourceKey();
	
	@Autowired
	ClusteredSchedulerService schedulerService; 
	
	@Autowired
	RealmService realmService; 
	
	@Autowired
	CacheService cacheService; 
	
	@PostConstruct 
	private void postConstruct() {
		
		String jobKey = getJobKey();
		JobDataMap data = new PermissionsAwareJobData(jobKey);
		data.put("clz", getClass().getInterfaces()[0]);
		try {
			if(!schedulerService.jobExists(jobKey)) {
				schedulerService.scheduleIn(BatchJob.class, jobKey, data , getBatchInterval(), getBatchInterval());
			}
		} catch (SchedulerException e) {
			log.error("Failed to schedule batch job", e);
		}
	}
	
	protected String getJobKey() {
		return String.format("batch-%s", getResourceKey());
	}

	
	@SuppressWarnings("unchecked")
	@Override
	public synchronized void processBatchItems() {
		
		
		Cache<String,Boolean> cache = cacheService.getCacheOrCreate(getJobKey(), String.class, Boolean.class);
		Boolean running = cache.get(getJobKey());
		if(Boolean.TRUE.equals(running)) {
			log.info(String.format("Existing batch job for %s is currently in progress", getJobKey()));
			return;
		}
		
		cache.put(getJobKey(), Boolean.TRUE);
		
		try {
			/**
			 * We now get all resources and mark them as deleted to prevent further invocations
			 * of this batch job rescheduling the batch item. Once this method returns the
			 * batch item should never be returned again.
			 */
			Collection<T> items = getRepository().getAllResourcesAndMarkDeleted();
				
			if(items.isEmpty()) {
				if(log.isDebugEnabled()) {
					log.debug(String.format("There are no batch items to process"));
				}
				return;
			}
				
			if(log.isInfoEnabled()) {
				log.info(String.format("Processing %d batch items", items.size()));
			}
				
			for(T item : items) {
				
				boolean processed = true; 
				try {
					processed = process(item);			
				} catch(Throwable t) {
					log.error("Failed to process batch item", t);
					processed = onProcessFailure(item);
				} finally {
					if(processed) {
						try {
							getRepository().deleteResource(item);
						} catch (ResourceException e) {
							log.error("Failed to delete batch item resource", e);
						}
					}
				}
			}
		
		} finally {
			cache.remove(getJobKey());
		}
		
	}

	protected boolean onProcessFailure(T item) {
		return true;
	}

	
}
