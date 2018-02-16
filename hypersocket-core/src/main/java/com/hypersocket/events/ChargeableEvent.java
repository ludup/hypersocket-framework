package com.hypersocket.events;

import com.hypersocket.realm.Realm;

public interface ChargeableEvent {

	Realm getCurrentRealm();
	
	boolean isSuccess();
	
	Double getCharge();
}
