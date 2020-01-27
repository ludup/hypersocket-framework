package com.hypersocket.password.policy;

import com.hypersocket.ApplicationContextServiceImpl;
import com.hypersocket.realm.PasswordCreator;
import com.hypersocket.realm.Principal;
import com.hypersocket.resource.ResourceException;

public class PasswordPolicyPasswordCreator implements PasswordCreator {

	private String password;
	
	@Override
	public char[] createPassword(Principal principal) throws ResourceException {
		PasswordPolicyResourceService service = ApplicationContextServiceImpl.getInstance().getBean(PasswordPolicyResourceService.class);
		PasswordPolicyResource policy = service.resolvePolicy(principal);
		return (password = service.generatePassword(policy)).toCharArray();
	}

	public String getPassword() {
		return password;
	}
}
