package com.hypersocket.auth;

import com.hypersocket.session.Session;

public interface AuthenticationStateListener {

	void logonComplete(Session session);
}
