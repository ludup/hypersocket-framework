package com.hypersocket.session;

import org.apache.commons.lang3.RandomStringUtils;

public class SessionResourceToken<T> {

	private Session session;
	private String shortCode;
	private long timestamp = System.currentTimeMillis();
	private T resource;
	
	public SessionResourceToken(Session session, T resource) {
		this.session = session;
		this.resource = resource;
		this.shortCode = SessionServiceImpl.TOKEN_PREFIX + RandomStringUtils.randomAlphanumeric(16);
	}
	
	public Session getSession() {
		return session;
	}

	public String getShortCode() {
		return shortCode;
	}

	public T getResource() {
		return resource;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
}
