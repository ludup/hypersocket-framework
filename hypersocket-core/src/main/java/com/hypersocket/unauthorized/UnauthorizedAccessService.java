package com.hypersocket.unauthorized;

import java.io.IOException;

import com.hypersocket.auth.AuthenticationAttemptEvent;
import com.hypersocket.realm.LogonException;
import com.hypersocket.realm.Realm;

public interface UnauthorizedAccessService {

	void onAuthenticationFailure(AuthenticationAttemptEvent event);

	boolean verifyPassword(Realm realm, String scheme, String principal, char[] password)
			throws LogonException, IOException;

}
