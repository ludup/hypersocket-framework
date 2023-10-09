package com.hypersocket.remoteservices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.json.version.HypersocketVersion;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;

@Component
public class DefaultRemoteServiceIdentificationProvider implements RemoteServiceIdentificationProvider {
	
	@Autowired
	private RealmService realmService;

	public DefaultRemoteServiceIdentificationProvider() {
	}

	@Override
	public String getAccountName(Realm realm) {
		return realmService.getRealmProperty(realm, "registration.company");
	}

	@Override
	public String getAccountID(Realm realm) {
		return HypersocketVersion.getSerial();
	}

	@Override
	public String getAccountUser(Realm realm) {
		return realmService.getRealmProperty(realm, "registration.name");
	}

	@Override
	public String getAccountEmail(Realm realm) {
		return realmService.getRealmProperty(realm, "registration.email");
	}

	@Override
	public InstanceType getInstanceType(Realm realm) {
		if(realm.equals(realmService.getSystemRealm()))
			return InstanceType.SYSTEM_REALM;
		else {
			return InstanceType.OTHER_REALM;
		}
	}
}
