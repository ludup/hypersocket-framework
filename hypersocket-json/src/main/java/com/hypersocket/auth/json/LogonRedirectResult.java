package com.hypersocket.auth.json;

import com.hypersocket.json.AuthenticationRedirectResult;
import com.hypersocket.json.version.HypersocketVersion;

public class LogonRedirectResult extends AuthenticationRedirectResult {
	{
		version = HypersocketVersion.getVersion("com.hypersocket/hypersocket-json");
	}

	public LogonRedirectResult() {
		super();
	}

	public LogonRedirectResult(String bannerMsg, String errorMsg, String errorStyle, boolean showLocales,
			String location) {
		super(bannerMsg, errorMsg, errorStyle, showLocales, location);
	}

}
