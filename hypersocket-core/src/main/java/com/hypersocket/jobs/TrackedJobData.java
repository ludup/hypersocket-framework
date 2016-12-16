package com.hypersocket.jobs;

import java.util.Locale;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.scheduler.PermissionsAwareJobData;

public class TrackedJobData extends PermissionsAwareJobData {

	private static final long serialVersionUID = -2828631488989447503L;

	public TrackedJobData(String jobResourceKey, String uuid) {
		super(jobResourceKey);
		put("trackingUUID", uuid);
	}
	
	public TrackedJobData(Realm currentRealm, String jobResourceKey, String uuid) {
		super(currentRealm, jobResourceKey);
		put("trackingUUID", uuid);
	}

	public TrackedJobData(Realm currentRealm, Principal principal, 
			Locale locale, String jobResourceKey, String uuid) {
		super(currentRealm, principal, locale, jobResourceKey);
		put("trackingUUID", uuid);
	}
}
