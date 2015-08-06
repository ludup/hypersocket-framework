package com.hypersocket.client.rmi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ResourceLauncherThread extends Thread {

	static private Logger log = LoggerFactory.getLogger(ResourceLauncherThread.class);
	ResourceLauncher launcher;
	
	public ResourceLauncherThread(ResourceLauncher launcher) {
		this.launcher = launcher;
		start();
	}

	public void run() {
		
		try {
		
			int exitCode = launcher.launch();
			log.info("Launcher returned " + exitCode);
		
		} catch(Throwable t) {
			log.error("Failed to launch", t);
		}
	}

}
