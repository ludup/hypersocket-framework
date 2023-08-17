package com.hypersocket.scheduler;

import java.util.Locale;

import org.quartz.JobDataMap;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.session.Session;

public class PermissionsAwareJobData extends JobDataMap {

	private static final long serialVersionUID = -2512176436464687235L;
		
	public PermissionsAwareJobData(String jobResourceKey, Object... args) {
		this(null, null, null, null, jobResourceKey, args);
	}
	
	public PermissionsAwareJobData(Realm currentRealm, String jobResourceKey, Object... args) {
		this(null, currentRealm, null, null, jobResourceKey, args);
	}
	
	public PermissionsAwareJobData(Session session, String jobResourceKey, Object... args) {
		this(session, null, null, null, jobResourceKey, args);
	}
	public PermissionsAwareJobData(Realm currentRealm, Principal principal, Locale locale, String jobResourceKey, Object... args) {
		this(null, currentRealm, principal, locale, jobResourceKey, args);
	}
	
	public String getJobName() {
		return getString("jobName");
	}
	
	public PermissionsAwareJobData(Session session, Realm currentRealm, Principal principal, Locale locale, String jobResourceKey, Object... args) {
		put(JobData.KEY_JOB_NAME, jobResourceKey);
		put(JobData.KEY_JOB_ARGS, args);
		put("permissions", Boolean.TRUE);
		
		if(session!=null) {
			put("session", session.getId());
		}
		if(currentRealm!=null) {
			put("realm", currentRealm.getId());
		}
		if(principal!=null) {
			put("principal", principal.getId());
		}
		if(locale!=null) {
			put("locale", locale.getLanguage());
		}
	}
	
}
