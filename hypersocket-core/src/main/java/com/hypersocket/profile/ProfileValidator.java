package com.hypersocket.profile;

import java.util.Collection;

import com.hypersocket.auth.AuthenticationModulesOperationContext;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;

public interface ProfileValidator {

	int getMaximumCompletedAuths(Realm realm);
	
	Collection<String> getRequiredUserCredentials(Principal principal, AuthenticationModulesOperationContext ctx);

	Collection<String> getRequired2FACredentials(Principal principal) throws AccessDeniedException;
}
