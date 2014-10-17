package com.hypersocket.tests.logon;

import org.apache.http.client.ClientProtocolException;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hypersocket.tests.AbstractServerTest;

public class NoPermissionTest extends AbstractServerTest {

	@BeforeClass
	public static void init() throws Exception {
		logon("System", "admin", "Password123?");
		createUser("System", "user1", "password1", false);
		logoff();

	}

	@Test(expected = AssertionError.class)
	public void testLogin() throws Exception {
		logon("System", "user1", "password1");
	}

	@Test(expected = ClientProtocolException.class)
	public void testLogoutWithoutLogin() throws Exception {
		logoff();
	}

}
