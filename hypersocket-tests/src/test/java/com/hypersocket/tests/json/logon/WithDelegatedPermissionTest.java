package com.hypersocket.tests.json.logon;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.BeforeClass;
import org.junit.Test;

import com.hypersocket.auth.AuthenticationPermission;
import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.json.JsonRoleResourceStatus;
import com.hypersocket.json.JsonSession;
import com.hypersocket.realm.UserPermission;
import com.hypersocket.tests.AbstractServerTest;

public class WithDelegatedPermissionTest extends AbstractServerTest {

	@BeforeClass
	public static void init() throws Exception {
		logon("System", "admin", "Password123?");
		JsonResourceStatus jsonCreateUser = createUser("System", "user1",
				"password1", false);
		Long[] permissions = {
				getPermissionId(AuthenticationPermission.LOGON.getResourceKey()),
				getPermissionId(UserPermission.CREATE.getResourceKey()) };
		JsonRoleResourceStatus jsonCreateRole = createRole("newrole",
				permissions);
		addUserToRole(jsonCreateRole.getResource(), jsonCreateUser);
		logoff();
	}

	@Test
	public void testDelegateLogin() throws Exception {
		logon("System", "user1", "password1");
		JsonSession session=getSession();
	    assertNotNull(session);
	    assertEquals("user1", session.getCurrentPrincipal().getPrincipalName());
	    logoff();
	}
	
	@Test(expected = AssertionError.class)
	public void testUserLoginWithIncorrectPassword() throws Exception {
		logon("System", "user1", "password");
	}
}
