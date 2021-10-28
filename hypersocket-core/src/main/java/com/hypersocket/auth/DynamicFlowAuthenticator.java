package com.hypersocket.auth;

import java.util.Collection;
import java.util.Set;

import com.hypersocket.realm.Principal;

public interface DynamicFlowAuthenticator extends Authenticator {

	Collection<String> resolveAuthenticators(AuthenticationScheme scheme, Set<AuthenticationScheme> ignoreSchemes, Principal principal);

}
