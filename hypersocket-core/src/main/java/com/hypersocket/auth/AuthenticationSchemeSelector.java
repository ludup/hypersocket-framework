package com.hypersocket.auth;

import java.util.Map;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;

public interface AuthenticationSchemeSelector {
	AuthenticationScheme selectScheme(String schemeResourceKey, String remoteAddress,
			Map<String, Object> environment, Realm realm) throws AccessDeniedException;
}
