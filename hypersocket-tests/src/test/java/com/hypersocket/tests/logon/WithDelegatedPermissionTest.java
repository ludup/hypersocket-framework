package com.hypersocket.tests.logon;

import org.junit.BeforeClass;
import org.junit.Test;

import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.json.JsonRoleResourceStatus;
import com.hypersocket.tests.AbstractServerTest;

public class WithDelegatedPermissionTest extends AbstractServerTest {
	
	
	@BeforeClass
	public static void init()throws Exception{
		logon("Default", "admin", "Password123?");
		JsonResourceStatus jsonCreateUser=createUser("Default","user1","password1",false);
		Long[] permissions = {getPermissionId("permission.logon"),getPermissionId("user.create") };
		JsonRoleResourceStatus jsonCreateRole = createRole("newrole", permissions);
		addUserToRole(jsonCreateRole.getResource(), jsonCreateUser);
		logoff();
	}
	
	@Test
	public void testDelegateLogin() throws Exception{
		logon("Default", "user1","password1");
	}
}
