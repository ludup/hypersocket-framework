package com.hypersocket.scheduler;

import java.util.Date;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobKey;

import com.hypersocket.utils.HypersocketUtils;

public class SchedulerResource {
	private String name;
	private String group;
	private String id;
	private Date nextFire;
	private Date lastFire;
	private long timeTaken;
	private String description;
	private SchedulerJobState status = SchedulerJobState.MISSING;
	private String error;
	private String trace;
	private Properties data = new Properties();
	private String realm;
	private Long realmId;

	public SchedulerResource() {
	}

	public SchedulerResource(JobKey k) {
		name = k.getName();
		group = k.getGroup();
	}

	public SchedulerResource(Date nextFire, Date lastFire, JobKey jobKey, SchedulerJobState status) {
		/* NOTE: At the moment, job groups are not used, so ID is same as Name */
		setId(HypersocketUtils.base64Encode(jobKey.getName()));
		setGroup(jobKey.getGroup());
		setName(jobKey.getName());
		setLastFire(lastFire);
		setNextFire(nextFire);
		this.status = status;
	}
	
	public long getTimeTaken() {
		return timeTaken;
	}

	public void setTimeTaken(long timeTaken) {
		this.timeTaken = timeTaken;
	}

	public String getRealm() {
		return realm;
	}

	public void setRealm(String realm) {
		this.realm = realm;
	}

	public Properties getData() {
		return data;
	}

	public String getName() {
		return name;
	}

	public Long getRealmId() {
		return realmId;
	}

	public void setRealmId(Long realmId) {
		this.realmId = realmId;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Date getNextFire() {
		return nextFire;
	}

	public void setNextFire(Date nextFire) {
		this.nextFire = nextFire;
	}

	public Date getLastFire() {
		return lastFire;
	}

	public void setLastFire(Date lastFire) {
		this.lastFire = lastFire;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public JobKey getJobKey() {
		return new JobKey(name, group);
	}

	public SchedulerJobState getStatus() {
		return status;
	}

	public void setStatus(SchedulerJobState status) {
		this.status = status;
	}

	public boolean matches(String searchPattern, String searchColumn) {
		/* TODO Support columns properly */
		return StringUtils.isBlank(searchPattern)
				|| (StringUtils.isNotBlank(searchPattern) && matchesString(id, searchPattern))
				|| (StringUtils.isNotBlank(searchPattern)  
						&& matchesString(name, searchPattern))
				|| (StringUtils.isNotBlank(searchPattern)  
						&& matchesString(group, searchPattern));
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public String getTrace() {
		return trace;
	}

	public void setTrace(String trace) {
		this.trace = trace;
	}

	private boolean matchesString(String text, String pattern) {
		return StringUtils.isNotBlank(text) && text.toLowerCase().contains(pattern.toLowerCase());
	}

}
