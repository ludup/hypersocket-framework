package com.hypersocket.tests.logon;


import org.apache.http.client.ClientProtocolException;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.tests.AbstractServerTest;

public class NoPermissionTest extends AbstractServerTest{

	@BeforeClass
	public static void init() throws Exception{
		logon("Default", "admin", "Password123?");
		JsonResourceStatus jsonCreateUser=createUser("Default","user1","password1",false);
		logoff();
		
	}
	
	@Test(expected=AssertionError.class)
	public void testLogin() throws Exception{
		logon("Default","user1","password1");
	}
	
	@Test(expected = ClientProtocolException.class)
	public void testLogoutWithoutLogin() throws Exception {
		logoff();
	}
	
}
