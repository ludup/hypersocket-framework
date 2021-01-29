package com.hypersocket.ip;

import java.net.InetAddress;

import com.hypersocket.realm.Realm;

public interface IPRestrictionProvider {

	boolean isAllowedAddress(InetAddress addr, String service, Realm realm);

}
