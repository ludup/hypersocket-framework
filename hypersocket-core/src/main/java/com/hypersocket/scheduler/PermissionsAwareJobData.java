package com.hypersocket.scheduler;

import java.util.Locale;

import org.quartz.JobDataMap;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.session.Session;

public class PermissionsAwareJobData extends JobDataMap {

	private static final long serialVersionUID = -2512176436464687235L;
		
	public PermissionsAwareJobData(Realm currentRealm, String jobResourceKey) {
		this(null, currentRealm, null, null, jobResourceKey);
	}
	
	public PermissionsAwareJobData(Session session, String jobResourceKey) {
		this(session, null, null, null, jobResourceKey);
	}
	public PermissionsAwareJobData(Realm currentRealm, Principal principal, Locale locale, String jobResourceKey) {
		this(null, currentRealm, principal, locale, jobResourceKey);
	}
	
	public String getJobName() {
		return getString("jobName");
	}
	
	public PermissionsAwareJobData(Session session, Realm currentRealm, Principal principal, Locale locale, String jobResourceKey) {
		
		put("jobName", jobResourceKey);
		
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
			put("locale", locale.toLanguageTag());
		}
	}
	
}
