package com.hypersocket.ip;

import java.net.InetAddress;
import java.net.UnknownHostException;

public interface IPRestrictionService {

	void registerListener(IPRestrictionListener listener);

	boolean isBlockedAddress(String addr) throws UnknownHostException;

	boolean isBlockedAddress(InetAddress addr);

	void blockIPAddress(String addr) throws UnknownHostException;

	void unblockIPAddress(String addr) throws UnknownHostException;

	void blockIPAddress(String addr, boolean permanent) throws UnknownHostException;

}
