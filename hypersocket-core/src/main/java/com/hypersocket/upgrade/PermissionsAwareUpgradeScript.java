package com.hypersocket.upgrade;

import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.session.SessionService;

public abstract class PermissionsAwareUpgradeScript implements Runnable {

	@Autowired
	private SessionService sessionService; 
	
	@Override
	public void run() {
		sessionService.runAsSystemContext(() -> doUpgrade());
	}
	
	protected abstract void doUpgrade();

}
