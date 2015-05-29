package com.hypersocket.client.rmi;

import java.io.Serializable;

@SuppressWarnings("serial")
public class BrowserLauncher implements ResourceLauncher, Serializable {

	public interface BrowserLauncherFactory {
		ResourceLauncher create(String uri);
	}

	private static BrowserLauncherFactory factory;

	public static void setFactory(BrowserLauncherFactory factory) {
		BrowserLauncher.factory = factory;
	}

	private String launchUri;

	public BrowserLauncher(String launchUri) {
		this.launchUri = launchUri;
	}

	@Override
	public int launch() {
		if (factory == null) {
			return new AWTBrowserLauncher(launchUri).launch();
		} else {
			return factory.create(launchUri).launch();
		}
	}

}
