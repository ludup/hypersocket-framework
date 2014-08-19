package com.hypersocket.tests.logon;

import org.junit.BeforeClass;
import org.junit.Test;

import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.json.JsonRoleResourceStatus;
import com.hypersocket.tests.AbstractServerTest;

public class UnauthorizedTest extends AbstractServerTest {
	
	@BeforeClass
	public static void init() throws Exception{
		logon("Default", "admin", "Password123?");
		JsonResourceStatus jsonCreateUser=createUser("Default","user1","password1",false);
		Long[] permissions = {getPermissionId("permission.logon") };
		JsonRoleResourceStatus jsonCreateRole = createRole("newrole", permissions);
		addUserToRole(jsonCreateRole.getResource(), jsonCreateUser);
		logoff();
	}
	
	
	@Test(expected=AssertionError.class)
    public void testLoginAdminWithIncorrectPassword() throws Exception{
    	logon("Default", "admin", "123");
    }
    
    @Test(expected=AssertionError.class)
    public void testUserLoginWithIncorrectPassword() throws Exception{
    	logon("Default","user1","password");
    }
	
}
