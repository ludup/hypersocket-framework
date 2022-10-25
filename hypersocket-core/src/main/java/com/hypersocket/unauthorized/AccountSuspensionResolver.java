package com.hypersocket.unauthorized;

import java.util.Set;

import com.hypersocket.realm.PrincipalWithoutPasswordResolver;
import com.hypersocket.realm.UserPrincipal;

public class AccountSuspensionResolver extends PrincipalWithoutPasswordResolver {

	public AccountSuspensionResolver(UserPrincipal<?> principal, int failedAttempts, int lockoutTime, int period) {
		super(principal);
		addToken("failedAttempts", failedAttempts);
		addToken("lockoutTime", lockoutTime);
		addToken("period", period);
	}

	public static Set<String> getVariables() {
		Set<String> results = PrincipalWithoutPasswordResolver.getVariables();
		results.add("failedAttempts");
		results.add("lockoutTime");
		results.add("period");
		return results;
	}
}
