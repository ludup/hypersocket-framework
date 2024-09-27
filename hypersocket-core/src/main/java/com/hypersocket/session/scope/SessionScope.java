package com.hypersocket.session.scope;

public class SessionScope {

	private final String scope;
	
	private final String source;
	
	public SessionScope(String scope, String source) {
		this.scope = scope;
		this.source = source;
	}

	public String getScope() {
		return scope;
	}

	public String getSource() {
		return source;
	}

}
