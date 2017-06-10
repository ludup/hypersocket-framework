package com.hypersocket.realm;

import java.util.Set;

public class PrincipalWithPasswordResolver extends PrincipalWithoutPasswordResolver {

	
	public PrincipalWithPasswordResolver(Principal principal, String password) {
		super(principal);
		addToken("password", password);
	}

	public static Set<String> getVariables() {
		Set<String> results = PrincipalWithoutPasswordResolver.getVariables();
		results.add("password");
		return results;
	}
}
