package com.hypersocket.auth;

public class InvalidAuthenticationContext extends RuntimeException {

	private static final long serialVersionUID = 8089721650943141494L;

	public InvalidAuthenticationContext() {
	}

	public InvalidAuthenticationContext(String arg0) {
		super(arg0);
	}

	public InvalidAuthenticationContext(Throwable arg0) {
		super(arg0);
	}

	public InvalidAuthenticationContext(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

}
