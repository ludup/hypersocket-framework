package com.hypersocket.account.linking;

import com.hypersocket.realm.Principal;
import com.hypersocket.utils.StaticResolver;

public class AccountResolver extends StaticResolver {

	public AccountResolver(Principal primary, Principal secondary, String password) {
		this(primary, secondary);
		addToken("password", password);
	}
	
	public AccountResolver(Principal primary, Principal secondary) {
		addToken("principalName", primary.getPrincipalName());
		addToken("principalDesc", primary.getPrincipalDescription());
		addToken("principalRealm", primary.getRealm().getName());
		addToken("linkedName", secondary.getPrincipalName());
		addToken("linkedDesc", secondary.getPrincipalDescription());
		addToken("linkedRealm", secondary.getRealm().getName());
	}

}
