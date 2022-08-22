package com.hypersocket.auth.json;

import com.hypersocket.json.version.HypersocketVersion;
import com.hypersocket.permissions.Role;
import com.hypersocket.session.Session;

public class LogonSuccessResult extends AuthenticationSuccessResult {
	{
		version = HypersocketVersion.getVersion("com.hypersocket/hypersocket-json");
	}

	public LogonSuccessResult() {
		super();
	}

	public LogonSuccessResult(String error, String errorStyle, boolean showLocales, Session session, String homePage,
			Role currentRole) {
		super(error, errorStyle, showLocales, session, homePage, currentRole);
	}

	public LogonSuccessResult(String bannerMsg, boolean showLocales, Session session, String homePage,
			Role currentRole) {
		super(bannerMsg, showLocales, session, homePage, currentRole);
	}
}
