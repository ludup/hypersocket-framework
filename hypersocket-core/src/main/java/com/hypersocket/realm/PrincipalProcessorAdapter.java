package com.hypersocket.realm;

import java.util.Map;

import com.hypersocket.resource.ResourceException;

public class PrincipalProcessorAdapter implements PrincipalProcessor {

	@Override
	public void beforeUpdate(Principal principal, Map<String, String> properties)  throws ResourceException {

	}

	@Override
	public void afterUpdate(Principal principal, Map<String, String> properties) throws ResourceException {

	}

	@Override
	public void afterCreate(Principal principal, String password, Map<String, String> properties) throws ResourceException {

	}

	@Override
	public void beforeChangePassword(Principal principal, String newPassword, String oldPassword) throws ResourceException{
		
	}

	@Override
	public void afterChangePassword(Principal principal, String newPassword, String oldPassword) throws ResourceException {

	}

	@Override
	public void beforeCreate(Realm realm, String realmModule, String username, Map<String, String> properties) throws ResourceException {
		
	}

	@Override
	public void beforeSetPassword(Principal principal, String password) throws ResourceException {

	}

	@Override
	public void afterSetPassword(Principal principal, String password) throws ResourceException {

	}

}
