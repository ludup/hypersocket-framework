package com.hypersocket.tests;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

public class LoginTests extends AbstractServerTest {

	@Test(expected = ClientProtocolException.class)
	public void logoutWithoutLogin() throws Exception {
		logoff();
	}

	@Test
	public void loginAndLogout() throws Exception {
		logon("System", "admin", "Password123?");
		logoff();
	}

	@Test(expected = AssertionError.class)
	public void loginWithBadPassword() throws Exception {
		logon("System", "admin", "badPassword");
		logoff();
	}

//	@Test(expected = AssertionError.class)
//	public void loginWithBadRealm() throws Exception {
//		logon("InvalidRealm", "admin", "Password123?");
//		logoff();
//	}
	
	@Test(expected = AssertionError.class)
	public void loginWithBadUser() throws Exception {
		logon("System", "userDoesNotExist", "password");
		logoff();
	}
}
