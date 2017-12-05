package com.hypersocket.realm;

import com.hypersocket.resource.ResourceException;

public interface PasswordCreator {

	char[] createPassword(Principal principal) throws ResourceException;

	String getPassword();
}
