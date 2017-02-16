package com.hypersocket.realm;

import com.hypersocket.utils.StaticResolver;

public class PrincipalResolver extends StaticResolver {

	public PrincipalResolver(Principal principal, String password) {
		super();
		addToken("principalName", principal.getPrincipalName());
		addToken("principalDesc", principal.getPrincipalDescription());
		addToken("principalRealm", principal.getRealm().getName());
		addToken("password", password);
	}
}
