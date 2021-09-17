package com.hypersocket.batch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.cache.Cache;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.hibernate.exception.LockAcquisitionException;
import org.quartz.JobDataMap;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.cache.CacheService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmProvider;
import com.hypersocket.realm.RealmService;
import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.scheduler.ClusteredSchedulerService;
import com.hypersocket.scheduler.PermissionsAwareJobData;
import com.hypersocket.tables.ColumnSort;

public abstract class BatchProcessingServiceImpl<T extends AbstractEntity<Long>> implements BatchProcessingService<T> {

	static Logger log = LoggerFactory.getLogger(BatchProcessingServiceImpl.class);

	protected abstract BatchProcessingItemRepository<T> getRepository();

	protected abstract int getBatchInterval();

	protected abstract boolean process(T item) throws Exception;

	protected abstract String getResourceKey();

	@Autowired
	private ClusteredSchedulerService schedulerService;

	@Autowired
	private CacheService cacheService;

	@Autowired
	private RealmService realmService;

	@PostConstruct
	private void postConstruct() {

		String jobKey = getJobKey();
		JobDataMap data = new PermissionsAwareJobData(jobKey);
		data.put("clz", getClass().getInterfaces()[0]);
		try {
			if (!schedulerService.jobExists(jobKey)) {
				schedulerService.scheduleIn(BatchJob.class, jobKey, data, getBatchInterval(), getBatchInterval());
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
	@Transactional(rollbackFor = { LockAcquisitionException.class, IllegalStateException.class }, isolation = Isolation.SERIALIZABLE)
	public void processBatchItems() {

		synchronized (this) {

			Cache<String, Boolean> cache = cacheService.getCacheOrCreate(getJobKey(), String.class, Boolean.class);
			Boolean running = cache.get(getJobKey());
			if (Boolean.TRUE.equals(running)) {
				log.info(String.format("Existing batch job for %s is currently in progress", getJobKey()));
				return;
			}
			cache.put(getJobKey(), Boolean.TRUE);
		}

		try {
			if (log.isDebugEnabled()) {
				log.debug("Processing batch items");
			}

			
			List<Long> running = new ArrayList<>(); 
			for(Realm realm : realmService.allRealms()) {
				RealmProvider provider = realmService.getProviderForRealm(realm);
				if("reconcile.status.inprogress".equals(provider.getValue(realm, "realm.lastReconcileStatus"))) {
					log.info(String.format("Skipping batch items for realm %s because it is reconciling (based on 'lastReconcileStatus')", realm.getName()));
					running.add(realm.getId());
				}
			}
			
			
			int succeeded = 0;
			int failed = 0;

			/*
			 * Mark all entries as deleted. This will cause them to lock, and so if this
			 * process is run again, then that transaction will lock entirely until this one
			 * is complete, by which time all of the deleted rows will have been entirely
			 * removed.
			 * 
			 */
			try {
				getRepository().markAllAsDeleted(running, true);
			} catch (LockAcquisitionException lae) {
				log.warn(
						"Database locked, the batch process cannot complete. This is likely a running synchronize, or other job that has written a realm that has a batch item ready.");
				throw lae;
			}

			/* Now iterate over all those that were marked as deleted */
			Iterator<T> itemIt = (Iterator<T>) getRepository().iterate(getRepository().getEntityClass(),
					new ColumnSort[] {}, new DeletedCriteria(true), new CriteriaConfiguration() {
						
						@Override
						public void configure(Criteria criteria) {
							if(!running.isEmpty())
								criteria.add(Restrictions.not(Restrictions.in("realm", running)));
						}
					});
			while (itemIt.hasNext()) {
				T item = itemIt.next();
				boolean processed = true;
				try {
					processed = process(item);
				} catch (Throwable t) {
					log.error("Failed to process batch item", t);
					processed = onProcessFailure(item, t);
				} finally {
					if (processed) {
						/* Finally physically remove the item */
						itemIt.remove();
						succeeded++;
					} else
						failed++;
				}
			}

			/* Anything thats left over, remove the deleted flag so it is 
			 * picked up again on the cycle. Note this is not strictly required,
			 * just for neatness
			 */
			getRepository().markAllAsDeleted(running, false);

			if (log.isDebugEnabled()) {
				log.debug(String.format("Processed batch items. %d processed and deleted, %d failed and deleted.",
						succeeded, failed));
			}
		} catch (AccessDeniedException e) {
			throw new IllegalStateException("Failed to process batch items.", e);
		} finally {
			synchronized (this) {
				Cache<String, Boolean> cache = cacheService.getCacheOrCreate(getJobKey(), String.class, Boolean.class);
				cache.remove(getJobKey());
			}
		}

	}

	protected boolean onProcessFailure(T item, Throwable exception) {
		return true;
	}

}
