package com.hypersocket.realm;

import java.util.Set;

public class PrincipalWithPasswordResolver extends PrincipalWithoutPasswordResolver {

	
	public PrincipalWithPasswordResolver(UserPrincipal principal, String password, boolean temporary) {
		super(principal);
		addToken("password", password);
		addToken("temporary", temporary);
	}

	public static Set<String> getVariables() {
		Set<String> results = PrincipalWithoutPasswordResolver.getVariables();
		results.add("password");
		results.add("temporary");
		return results;
	}
}
