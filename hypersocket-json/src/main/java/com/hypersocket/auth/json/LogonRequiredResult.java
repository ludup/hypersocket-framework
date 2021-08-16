package com.hypersocket.auth.json;

import java.util.Map;

import com.hypersocket.auth.AuthenticationRequiredResult;
import com.hypersocket.input.FormTemplate;
import com.hypersocket.json.version.HypersocketVersion;
import com.hypersocket.realm.Realm;

public class LogonRequiredResult extends AuthenticationRequiredResult {
	{
		version = HypersocketVersion.getVersion("com.hypersocket/hypersocket-json");
	}

	public LogonRequiredResult() {
		super();
	}

	public LogonRequiredResult(String bannerMsg, String errorMsg, String errorStyle, boolean lastErrorIsResourceKey,
			FormTemplate formTemplate, boolean showLocales, boolean isNew, boolean isFirst, boolean isLast,
			boolean lastResultSuccessful, boolean inPostAuthentication, String lastButtonResourceKey, Realm realm,
			Map<String, String[]> requestParameters) {
		super(bannerMsg, errorMsg, errorStyle, lastErrorIsResourceKey, formTemplate, showLocales, isNew, isFirst, isLast,
				lastResultSuccessful, inPostAuthentication, lastButtonResourceKey, realm, requestParameters);
	}

	public LogonRequiredResult(String bannerMsg, String errorMsg, String errorStyle, boolean lastErrorIsResourceKey,
			FormTemplate formTemplate, boolean showLocales, boolean isNew, boolean isFirst, boolean isLast,
			boolean lastResultSuccessful, boolean inPostAuthentication, String lastButtonResourceKey, Realm realm) {
		super(bannerMsg, errorMsg, errorStyle, lastErrorIsResourceKey, formTemplate, showLocales, isNew, isFirst, isLast,
				lastResultSuccessful, inPostAuthentication, lastButtonResourceKey, realm);
	}
}
