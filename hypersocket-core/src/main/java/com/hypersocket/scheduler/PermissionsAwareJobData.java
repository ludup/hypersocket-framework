package com.hypersocket.scheduler;

import java.util.Locale;

import org.quartz.JobDataMap;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;

public class PermissionsAwareJobData extends JobDataMap {

	private static final long serialVersionUID = -2512176436464687235L;
	Realm currentRealm;
	Principal principal;
	Locale locale;
	String jobType;
	String name;
	
	public PermissionsAwareJobData(Realm currentRealm) {
		this(currentRealm, null, null);
	}
	
	public PermissionsAwareJobData(Realm currentRealm, Principal principal, Locale locale) {
		this.currentRealm = currentRealm;
		this.principal = principal;
		this.locale = locale;
		put("realm", currentRealm);
		if(principal!=null) {
			put("principal", principal);
		}
		if(locale!=null) {
			put("locale", locale);
		}
	}
	
	public Realm getCurrentRealm() {
		return currentRealm;
	}

	public Principal getCurrentPrincipal() {
		return principal;
	}

	public Locale getLocale() {
		return locale;
	}

	public String getJobType() {
		return jobType;
	}

	public void setJobType(String jobType) {
		this.jobType = jobType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	

}
