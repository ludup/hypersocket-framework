package com.hypersocket.ip;

public interface IPRestrictionListener {

	void onBlockIP(String addr);
	
	void onUnblockIP(String addr);
}
