package com.hypersocket.auth.json;

import com.hypersocket.json.version.HypersocketVersion;
import com.hypersocket.session.Session;

public class LogonSuccessResult extends AuthenticationSuccessResult {
	{
		version = HypersocketVersion.getVersion("com.hypersocket/hypersocket-json");
	}

	public LogonSuccessResult() {
		super();
	}

	public LogonSuccessResult(String bannerMsg, boolean showLocales, Session session, String homePage) {
		super(bannerMsg, showLocales, session, homePage);
	}
}
