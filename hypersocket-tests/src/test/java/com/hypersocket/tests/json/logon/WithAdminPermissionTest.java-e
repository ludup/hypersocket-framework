package com.hypersocket.tests.json.logon;

import static org.junit.Assert.*;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.hypersocket.json.JsonSession;
import com.hypersocket.tests.AbstractServerTest;

public class WithAdminPermissionTest extends AbstractServerTest{

	@Test(expected = ClientProtocolException.class)
	public void testLogoutWithoutLogin() throws Exception {
		logoff();
	}
		
	@Test
	public void testAdminLogin()throws Exception{
		logon("System","admin" ,"Password123?");
	    JsonSession session=getSession();
	    assertNotNull(session);
	    assertEquals("admin", session.getCurrentPrincipal().getPrincipalName());
		logoff();
	}
	
	@Test
	public void testLoginAndLogout() throws Exception {
		logon("System", "admin", "Password123?");
		logoff();
		assertNull(getSession());
	}
	
	@Test(expected = AssertionError.class)
	public void testLoginAdminWithIncorrectPassword() throws Exception {
		logon("System", "admin", "123");
	}
	
    
}
