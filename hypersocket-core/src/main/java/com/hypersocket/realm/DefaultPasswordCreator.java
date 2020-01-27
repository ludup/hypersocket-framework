package com.hypersocket.realm;

import com.hypersocket.resource.ResourceException;

public class DefaultPasswordCreator implements PasswordCreator {

	private String password;
	
	public DefaultPasswordCreator(String password) {
		this.password = password;
	}
	
	@Override
	public char[] createPassword(Principal principal) throws ResourceException {
		return password.toCharArray();
	}

	@Override
	public String getPassword() {
		return password;
	}

}
