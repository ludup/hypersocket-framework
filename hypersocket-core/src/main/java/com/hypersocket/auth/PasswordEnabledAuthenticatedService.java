package com.hypersocket.auth;

import com.hypersocket.session.Session;

public interface PasswordEnabledAuthenticatedService extends AuthenticatedService {
	
	String getCurrentPassword();

	void setCurrentPassword(String password);
	
	void setCurrentPassword(Session session, String password);
}
