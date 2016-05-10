package com.hypersocket.auth;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalType;

public class FakePrincipal extends Principal {

	public FakePrincipal() {
		this.setId(-1L);
		this.setName("user1");
	}
	
	public FakePrincipal(String username) {
		this.setId(-1L);
		this.setName(username);
	}
	
	@Override
	public PrincipalType getType() {
		return PrincipalType.USER;
	}
	
	public String getPrincipalDescription() {
		return getName();
	}
}
