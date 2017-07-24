package com.hypersocket.profile;

import com.hypersocket.realm.Realm;

public interface ProfileBatchChangeEvent {
	boolean isSuccess();
	
	Realm getCurrentRealm();
}
