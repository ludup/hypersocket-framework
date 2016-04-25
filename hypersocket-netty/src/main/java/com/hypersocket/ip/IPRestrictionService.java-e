package com.hypersocket.ip;

import java.net.InetAddress;
import java.net.UnknownHostException;

public interface IPRestrictionService {

	boolean isBlockedAddress(String addr) throws UnknownHostException;

	boolean isBlockedAddress(InetAddress addr);

	void blockIPAddress(String addr) throws UnknownHostException;

	void unblockIPAddress(String addr) throws UnknownHostException;

	void blockIPAddress(String addr, boolean permanent) throws UnknownHostException;

	boolean isAllowedAddress(InetAddress addr);

	void disallowIPAddress(String addr) throws UnknownHostException;

	void allowIPAddress(String addr) throws UnknownHostException;

	boolean isAllowedAddress(String ip) throws UnknownHostException;

	void clearRules(boolean allowed, boolean blocked);

}
