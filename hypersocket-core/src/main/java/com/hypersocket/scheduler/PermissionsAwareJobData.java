package com.hypersocket.scheduler;

import org.quartz.JobDataMap;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;

public class PermissionsAwareJobData extends JobDataMap {

	private static final long serialVersionUID = -2512176436464687235L;
	Realm currentRealm;
	Principal principal;
	
	public PermissionsAwareJobData(Realm currentRealm) {
		this(currentRealm, null);
	}
	
	public PermissionsAwareJobData(Realm currentRealm, Principal principal) {
		this.currentRealm = currentRealm;
		this.principal = principal;
		put("realm", currentRealm);
		if(principal!=null) {
			put("principal", principal);
		}
	}
	
	public Realm getCurrentRealm() {
		return currentRealm;
	}

	public Principal getCurrentPrincipal() {
		return principal;
	}

}
