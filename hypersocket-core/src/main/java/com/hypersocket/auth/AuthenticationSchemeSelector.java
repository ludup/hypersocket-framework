package com.hypersocket.auth;

import com.hypersocket.permissions.AccessDeniedException;

public interface AuthenticationSchemeSelector {
	AuthenticationScheme selectScheme(String schemeResourceKey, AuthenticationState state) throws AccessDeniedException;
}
