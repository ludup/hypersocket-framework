package com.hypersocket.realm;

import java.util.Map;

import com.hypersocket.resource.ResourceException;

public interface PrincipalProcessor {

	void beforeUpdate(Principal principal, Map<String,String> properties) throws ResourceException;
	
	void afterUpdate(Principal principal, Map<String,String> properties) throws ResourceException;

	void beforeCreate(Realm realm, String username, Map<String, String> properties) throws ResourceException;
	
	void afterCreate(Principal principal, Map<String, String> properties) throws ResourceException;
	
	void beforeChangePassword(Principal principal, String newPassword) throws ResourceException;
	
	void afterChangePassword(Principal principal, String newPassword) throws ResourceException;

	void beforeSetPassword(Principal principal, String password) throws ResourceException;

	void afterSetPassword(Principal principal, String password) throws ResourceException;

	
}
