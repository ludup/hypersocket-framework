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
		
	public PermissionsAwareJobData(Realm currentRealm, String jobResourceKey) {
		this(currentRealm, null, null, jobResourceKey);
	}
	
	public PermissionsAwareJobData(Realm currentRealm, Principal principal, Locale locale, String jobResourceKey) {
		this.currentRealm = currentRealm;
		this.principal = principal;
		this.locale = locale;
		put("realm", currentRealm);
		put("jobName", jobResourceKey);
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
	
}
