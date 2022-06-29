package com.hypersocket.server;

import java.net.InetAddress;

import com.hypersocket.realm.Realm;

public interface IPRestrictionProvider {

	boolean isAllowedAddress(InetAddress addr, String service, Realm realm);
	
	default int getWeight() {
		return 0;
	}

}
