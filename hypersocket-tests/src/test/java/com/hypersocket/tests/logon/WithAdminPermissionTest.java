package com.hypersocket.tests.logon;

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
		logon("Default","admin" ,"Password123?");
	    JsonSession session=getSession();
	    assertNotNull(session);
	    assertEquals("admin", session.getPrincipal().getPrincipalName());
		logoff();
	}
	
	@Test
	public void testLoginAndLogout() throws Exception {
		logon("Default", "admin", "Password123?");
		logoff();
		assertNull(getSession());
	}
	
    
}
