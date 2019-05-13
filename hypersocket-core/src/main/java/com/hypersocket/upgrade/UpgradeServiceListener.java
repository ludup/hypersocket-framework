package com.hypersocket.upgrade;

public interface UpgradeServiceListener {

	/**
	 * Run in transaction
	 */
	void onUpgradeFinished();
	
	/**
	 * Run outside of transaction
	 */
	void onUpgradeComplete();
}
