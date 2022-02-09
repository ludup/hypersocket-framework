package com.hypersocket.scheduler;

import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.upgrade.UpgradeService;
import com.hypersocket.upgrade.UpgradeServiceListener;

@Service
public class ClusteredSchedulerServiceImpl extends AbstractSchedulerServiceImpl implements 
		ClusteredSchedulerService {
	
	final static Logger LOG = LoggerFactory.getLogger(ClusteredSchedulerServiceImpl.class);

	@Autowired
	private Scheduler clusteredScheduler;
	
	@Autowired
	private UpgradeService upgradeService; 
	
	@Autowired
	private SessionFactory sessionFactory;
	
	protected Scheduler configureScheduler() throws SchedulerException {
		
		upgradeService.registerListener(new UpgradeServiceListener() {
			
			@Override
			public void onUpgradeComplete() {
				
				try {
					clusteredScheduler.start();
				} catch (SchedulerException e) {
					/**
					 * Should we be throwing this? It kills the server entirely.
					 */
					log.error("The clustered scheduler failed to start", e);
//					throw new IllegalStateException(e.getMessage(), e);
				}
			}

			@Override
			public void onUpgradeFinished() {
				/* Clean up jobs and triggers that no longer exist */
				Session session = sessionFactory.openSession();
				try {
					SQLQuery query = session.createSQLQuery("SELECT JOB_NAME, JOB_CLASS_NAME FROM QRTZ_JOB_DETAILS");			@SuppressWarnings("unchecked")
					List<Object[]> results = query.list();
					for(Object[] row : results) {
						try {
							Class.forName((String)row[1]);
						}
						catch(Exception e) {
							LOG.warn(String.format("Removing job %s as the class %s no longer exists.", row[0], row[1]), e);
							for(Trigger key : clusteredScheduler.getTriggersOfJob(new JobKey((String)row[0]))) {
								clusteredScheduler.unscheduleJob(key.getKey());
							}
							clusteredScheduler.deleteJob(new JobKey((String)row[0]));
						}
					}
				}
				catch(Exception e) {
					throw new IllegalStateException("Failed to clean up missing jobs.", e);
				}
				finally {
					session.close();
				}
				
			}
		});
		return clusteredScheduler;
	}
}
