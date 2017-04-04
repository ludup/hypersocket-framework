package com.hypersocket.auth;

import java.util.Date;

import com.hypersocket.local.LocalRealmProviderImpl;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalStatus;
import com.hypersocket.realm.PrincipalType;

public class FakePrincipal extends Principal {

	private static final long serialVersionUID = -4101680957356008739L;

	public FakePrincipal() {
		this.setId(-1L);
		this.setName("user1");
	}
	
	public FakePrincipal(String username) {
		this.setId(-1L);
		this.setName(username);
	}
	
	public String getIcon() {
		return "fa-database";
	}
	
	public PrincipalStatus getPrincipalStatus() {
		return PrincipalStatus.ENABLED;
	}
	
	public String getEmail() {
		return "";
	}
	
	@Override
	public PrincipalType getType() {
		return PrincipalType.USER;
	}
	
	public String getPrincipalDescription() {
		return getName();
	}
	
	public String getRealmModule() {
		return LocalRealmProviderImpl.REALM_RESOURCE_CATEGORY;
	}

	@Override
	public Date getExpires() {
		return null;
	}
}
