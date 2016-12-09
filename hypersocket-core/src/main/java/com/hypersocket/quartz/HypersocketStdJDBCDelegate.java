package com.hypersocket.quartz;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.JobPersistenceException;
import org.quartz.impl.JobDetailImpl;
import org.quartz.impl.jdbcjobstore.StdJDBCDelegate;
import org.quartz.spi.ClassLoadHelper;
import org.quartz.spi.OperableTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HypersocketStdJDBCDelegate extends StdJDBCDelegate{

    static Logger log = LoggerFactory.getLogger(HypersocketStdJDBCDelegate.class);

    @Override
    public JobDetail selectJobDetail(Connection conn, JobKey jobKey, ClassLoadHelper loadHelper) throws ClassNotFoundException, IOException, SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;

        try {
            ps = conn.prepareStatement(rtp(SELECT_JOB_DETAIL), ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ps.setString(1, jobKey.getName());
            ps.setString(2, jobKey.getGroup());
            rs = ps.executeQuery();

            JobDetailImpl job = null;

            if (rs.next()) {
                job = new JobDetailImpl();

                job.setName(rs.getString(COL_JOB_NAME));
                job.setGroup(rs.getString(COL_JOB_GROUP));
                job.setDescription(rs.getString(COL_DESCRIPTION));

                final String jobClassName = rs.getString(COL_JOB_CLASS);
                Class<? extends Job> jobClass = checkClassCanBeLoaded(conn, jobKey, loadHelper, jobClassName);

                job.setJobClass(jobClass);
                job.setDurability(getBoolean(rs, COL_IS_DURABLE));
                job.setRequestsRecovery(getBoolean(rs, COL_REQUESTS_RECOVERY));

                Map<?, ?> map = null;
                if (canUseProperties()) {
                    map = getMapFromProperties(rs);
                } else {
                    map = (Map<?, ?>) getObjectFromBlob(rs, COL_JOB_DATAMAP);
                }

                if (null != map) {
                    job.setJobDataMap(new JobDataMap(map));
                }

            }

            return job;
        } catch (JobPersistenceException e){
            throw new IllegalStateException(e.getMessage(), e);
        }finally {
            closeResultSet(rs);
            closeStatement(ps);
        }
    }

    private Class<? extends Job> checkClassCanBeLoaded(Connection conn, JobKey jobKey, ClassLoadHelper loadHelper, String className)
            throws ClassNotFoundException, JobPersistenceException, SQLException, IOException {
        try {
            return loadHelper.loadClass(className, Job.class);
        }catch (Exception e){
            log.error("Class {} could not be loaded, will be replaced by {}", className, NoOpJob.class.getName(), e);
            List<OperableTrigger> operableTriggers = selectTriggersForJob(conn, jobKey);
            for (OperableTrigger trigger: operableTriggers) {
                log.info("Following trigger {} in group {} for job {} in group {} will be marked in ERROR state.",
                        trigger.getKey().getName(), trigger.getKey().getGroup(), jobKey.getName(), jobKey.getGroup());
                updateTriggerState(conn, trigger.getKey(), STATE_ERROR);
            }
        }
        return loadHelper.loadClass(NoOpJob.class.getName(), Job.class);
    }

    private Map<?, ?> getMapFromProperties(ResultSet rs)
            throws ClassNotFoundException, IOException, SQLException {
        Map<?, ?> map;
        InputStream is = (InputStream) getJobDataFromBlob(rs, COL_JOB_DATAMAP);
        if(is == null) {
            return null;
        }
        Properties properties = new Properties();
        if (is != null) {
            try {
                properties.load(is);
            } finally {
                is.close();
            }
        }
        map = convertFromProperty(properties);
        return map;
    }
}
