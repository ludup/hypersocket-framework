package com.hypersocket.auth;

import java.util.Collection;

import com.hypersocket.realm.Principal;

public interface DynamicFlowAuthenticator extends Authenticator {

	Collection<AuthenticationModule> resolveAuthenticators(AuthenticationScheme scheme, Principal principal);

}
