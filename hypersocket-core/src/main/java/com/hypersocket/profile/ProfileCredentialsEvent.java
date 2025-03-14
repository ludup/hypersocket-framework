package com.hypersocket.profile;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.RealmService;

public interface ProfileCredentialsEvent {

	boolean isSuccess();
	
	Principal getTargetPrincipal(RealmService realmServide);
}
