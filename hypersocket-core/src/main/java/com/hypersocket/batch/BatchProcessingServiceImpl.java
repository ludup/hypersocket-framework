package com.hypersocket.batch;

import java.util.Iterator;

import javax.annotation.PostConstruct;
import javax.cache.Cache;

import org.hibernate.exception.LockAcquisitionException;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.cache.CacheService;
import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.scheduler.ClusteredSchedulerService;
import com.hypersocket.scheduler.PermissionsAwareJobData;
import com.hypersocket.tables.ColumnSort;

public abstract class BatchProcessingServiceImpl<T extends AbstractEntity<Long>> implements BatchProcessingService<T> {

	static Logger log = LoggerFactory.getLogger(BatchProcessingServiceImpl.class);
	
	protected abstract BatchProcessingItemRepository<T> getRepository();
	
	protected abstract int getBatchInterval();
	
	protected abstract boolean process(T item);
	
	protected abstract String getResourceKey();
	
	@Autowired
	private ClusteredSchedulerService schedulerService; 
	
	@Autowired
	private CacheService cacheService; 
	
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

	
	@Override
	@Transactional(isolation = Isolation.SERIALIZABLE)
	public  void processBatchItems() {
		
		/**
		 * Check for the status of this job running across the cluster. We don't want to run it again.
		 * 
		 * TODO: This should not be necessary any more due to transaction lock above
		 */
		synchronized(this) {
		
			Cache<String,Boolean> cache = cacheService.getCacheOrCreate(getJobKey(), String.class, Boolean.class);
			Boolean running = cache.get(getJobKey());
			if(Boolean.TRUE.equals(running)) {
				log.info(String.format("Existing batch job for %s is currently in progress", getJobKey()));
				return;
			}
			cache.put(getJobKey(), Boolean.TRUE);
		}
		
		
		try {
			if(log.isDebugEnabled()) {
				log.debug("Processing batch items");
			}
			
			int succeeded = 0;
			int failed = 0;
			
			/* Mark all entries as deleted. This will cause them to lock, and so if this process is
			 * run again, then that transaction will lock entirely until this one is complete, by
			 * which time all of the deleted rows will have been entirely removed.
			 * 
			 */
			try {
				getRepository().markAllAsDeleted();
			}
			catch(LockAcquisitionException lae) {
				log.warn("Database locked, the batch process cannot complete. This is likely a running synchronize, or other job that has written a realm that has a batch item ready.");
				return;
			}
			
			/* Now iterate over all those that were marked as deleted */
			for(@SuppressWarnings("unchecked") Iterator<T> itemIt = (Iterator<T>) getRepository().iterate(getRepository().getEntityClass(), new ColumnSort[] {}, new DeletedCriteria(true)); itemIt.hasNext(); ) {
				T item = itemIt.next();
				boolean processed = true; 
				try {
					processed = process(item);			
				} catch(Throwable t) {
					log.error("Failed to process batch item", t);
					//processed = onProcessFailure(item);
					onProcessFailure(item);
				} finally {
					 if(processed) { 
						 /* Finally physically remove the item */
						 itemIt.remove();
						 succeeded++;
					 }
					 else
						 failed++;
				}
			}

			if(log.isDebugEnabled()) {
				log.debug(String.format("Processed batch items. %d processed and deleted, %d failed and deleted.", succeeded, failed));
			}
		} finally {
			synchronized(this) {
				Cache<String,Boolean> cache = cacheService.getCacheOrCreate(getJobKey(), String.class, Boolean.class);
				cache.remove(getJobKey());
			}
		}
		
	}

	protected boolean onProcessFailure(T item) {
		return true;
	}

	
}
