package com.hypersocket.tests;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

public class LoginTests extends AbstractServerTest {

	@Test(expected=ClientProtocolException.class)
	public void logoutWithoutLogin() throws Exception {
		logoff();
	}
	
	@Test
	public void loginAndLogout() throws Exception {
		logon("Default", "admin", "Password123?");
		logoff();
	}
	
	@Test
	public void loginWithBadPassword() throws Exception {
		try {
			logon("Default", "admin", "badPassword");
			throw new AssertionError("User has logged on with bad password");
		} catch (Throwable e) {
		}
	}

	@Test
	public void loginWithBadUser() throws Exception {
		try {
			logon("Default", "userDoesNotExist", "password");
			throw new AssertionError("Non-existent User has logged on");
		} catch (Throwable e) {
		}
	}
}
