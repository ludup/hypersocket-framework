package com.hypersocket.server;

import java.net.UnknownHostException;

import com.hypersocket.realm.Realm;

public interface MutableIPRestrictionProvider extends IPRestrictionProvider {

	void denyIPAddress(Realm realm, String ipAddress, boolean permanent) throws UnknownHostException;
	
	void undenyIPAddress(Realm realm, String ipAddress) throws UnknownHostException;

	void clearRules(Realm realm, boolean allow, boolean deny);

	void allowIPAddress(Realm realm, String ipAddress, boolean permanent) throws UnknownHostException;

	void disallowIPAddress(Realm realm, String ipAddress, boolean permanent) throws UnknownHostException;

}
