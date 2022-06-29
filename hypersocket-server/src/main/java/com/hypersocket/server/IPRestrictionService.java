package com.hypersocket.server;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;

import com.hypersocket.realm.Realm;

public interface IPRestrictionService {

	String DEFAULT_SERVICE = "default";

	boolean isBlockedAddress(String addr, String service, Realm realm) throws UnknownHostException;

	boolean isBlockedAddress(InetAddress addr, String service, Realm realm);

	boolean isAllowedAddress(InetAddress addr, String service, Realm realm);

	boolean isAllowedAddress(String ip, String service, Realm realm) throws UnknownHostException;
	
	void registerService(IPRestrictionConsumer console);

	void registerProvider(IPRestrictionProvider provider);
	
	MutableIPRestrictionProvider getMutableProvider();
	
	Collection<IPRestrictionConsumer> getServices();

}
