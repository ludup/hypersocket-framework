package com.hypersocket.batch;

import java.util.Collection;

import javax.annotation.PostConstruct;

import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

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
	public void processBatchItems() {
		
		Collection<T> items = getRepository().allResources();
			
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
		
	}

	protected boolean onProcessFailure(T item) {
		return true;
	}

	
}
