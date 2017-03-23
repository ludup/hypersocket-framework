package com.hypersocket.realm;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.hypersocket.utils.StaticResolver;

public class PrincipalResolver extends StaticResolver {

	public PrincipalResolver(Principal principal, String password) {
		super();
		addToken("principalName", principal.getPrincipalName());
		addToken("principalDesc", principal.getPrincipalDescription());
		addToken("principalRealm", principal.getRealm().getName());
		addToken("password", password);
	}
	
	public static Set<String> getVariables() {
		return new HashSet<String>(Arrays.asList("principalName", 
				"principalDesc", "principalRealm", "password"));
	}
}
