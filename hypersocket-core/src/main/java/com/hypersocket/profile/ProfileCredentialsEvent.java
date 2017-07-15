package com.hypersocket.profile;

import com.hypersocket.realm.Principal;

public interface ProfileCredentialsEvent {

	boolean isSuccess();
	
	Principal getTargetPrincipal();
}
