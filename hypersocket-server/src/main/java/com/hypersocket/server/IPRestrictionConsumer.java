package com.hypersocket.server;

public class IPRestrictionConsumer {

	private String name;
	private String bundle;

	public IPRestrictionConsumer(String bundle, String name) {
		this.bundle = bundle;
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getBundle() {
		return bundle;
	}

}
