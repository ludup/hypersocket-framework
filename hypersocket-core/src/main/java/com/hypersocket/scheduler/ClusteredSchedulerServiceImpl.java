package com.hypersocket.scheduler;

import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.upgrade.UpgradeService;
import com.hypersocket.upgrade.UpgradeServiceListener;

@Service
public class ClusteredSchedulerServiceImpl extends AbstractSchedulerServiceImpl implements ClusteredSchedulerService {

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
				} catch (Exception e) {
					/**
					 * LDP - Should we be throwing this? It kills the server entirely.
					 * 
					 * BPS - Yes, because we want to know this and fix it. The server wont start any
					 * way due to similar errors being thrown later, or it might start but with
					 * broken jobs depending on exactly what is broken (jobs or triggers).
					 * 
					 * We first attempt to deleting the offending jobs / triggers in the code below.
					 * If this doesn't work, the best option is manual intervention.
					 * 
					 * It's better the problem be known about, rather jobs simply not running and
					 * nobody noticing. Serialization errors are most likely to occur after
					 * upgrades, so a server not starting after an upgrade has a better chance of
					 * being noticed.
					 */
					throw new IllegalStateException(
							"The clustered scheduler failed to start due to an unrecoverable error. Please correct this by removing all jobs at the database level and starting the server again. Contact support for assistence and quote this exact error.",
							e);
				}
			}

			@SuppressWarnings("unchecked")
			@Override
			public void onUpgradeFinished() {
				
				if("true".equals(System.getProperty("hypersocket.enableJobCleanup", "true"))) {
					Session session = sessionFactory.openSession();
					try {
						/* Clean up jobs and triggers that no longer exist */
						SQLQuery query = session.createSQLQuery("SELECT JOB_NAME, JOB_CLASS_NAME FROM QRTZ_JOB_DETAILS");
						List<Object[]> results = query.list();
						for (Object[] row : results) {
							try {
								Class.forName((String) row[1]);
							} catch (Exception e) {
								LOG.warn(String.format("Removing job %s as the class %s no longer exists.", row[0], row[1]),
										e);
								deleteJob(session, (String)row[0]);
							} 
						}
	
						/* Clean up triggers that cannot be deserialized */
						query = session.createSQLQuery("SELECT JOB_NAME, JOB_DATA, TRIGGER_NAME FROM QRTZ_TRIGGERS");
						results = query.list();
						for (Object[] row : results) { 
							try {
								ObjectInputStream ois = new ObjectInputStream(toStream(row[1]));
								ois.readObject();
							} catch(EOFException e) {
								// Ignore
							} catch (Exception e) {
								LOG.warn(String.format("Removing job %s as it is no longer readable.", row[0]),
										e);
								deleteJob(session, (String)row[0]);
							}
						}
					} catch (Exception e) {
						throw new IllegalStateException("Failed to clean up missing jobs. This will have to be manually corrected. Please correct this by removing all jobs at the database level and starting the server again. Contact support for assistence and quote this exact error.", e);
					} finally {
						session.close();
					}
				}

			}
			
			InputStream toStream(Object o) throws SQLException, EOFException {
				if(o instanceof Blob) {
					return ((Blob)o).getBinaryStream();
				}
				else if(o.getClass().isArray()) {
					byte[] o2 = (byte[])o;
					if(o2.length == 0)
						throw new EOFException();
					return new ByteArrayInputStream(o2);
				}
				else
					throw new UnsupportedOperationException("Cannot deserialize job data. This may be due to the database in use returning a different type for the serialised data that is expected. As a temporary work around, you can try disabling the clean up job by setting the system property 'hypersocket.enableJobCleanup' to true.");
			}
		});
		return clusteredScheduler;
	}
	
	private void deleteJob(Session session, String jobName) {
		SQLQuery innerQuery = session.createSQLQuery("SELECT TRIGGER_NAME FROM QRTZ_TRIGGERS WHERE JOB_NAME = '" + jobName + "'");
		List<Object[]> innerRow = innerQuery.list();
		if(innerRow.size() > 0) {							
			innerQuery = session.createSQLQuery("DELETE FROM QRTZ_SIMPLE_TRIGGERS WHERE TRIGGER_NAME = '" + innerRow.get(0) + "'");
			innerQuery.executeUpdate();
		}
		SQLQuery query = session.createSQLQuery("DELETE FROM QRTZ_TRIGGERS WHERE JOB_NAME = '" + jobName + "'");
		query.executeUpdate();
		query = session.createSQLQuery("DELETE FROM QRTZ_JOB_DETAILS WHERE JOB_NAME = '" + jobName + "'");
		query.executeUpdate();
	}
}
