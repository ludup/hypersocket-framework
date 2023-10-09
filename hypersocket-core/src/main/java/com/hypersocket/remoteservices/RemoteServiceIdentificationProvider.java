package com.hypersocket.remoteservices;

import com.hypersocket.realm.Realm;

public interface RemoteServiceIdentificationProvider {
	
	public enum InstanceType {
		 SYSTEM_REALM, TENANT_REALM, SUBSCRIBED_REALM, OTHER_REALM
	}
	
	InstanceType getInstanceType(Realm realm);

	String getAccountName(Realm realm);

	String getAccountID(Realm realm);

	String getAccountUser(Realm realm);

	String getAccountEmail(Realm realm);
}
