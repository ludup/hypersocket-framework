package com.hypersocket.tests.json.permissions;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hypersocket.auth.AuthenticationPermission;
import com.hypersocket.realm.UserPermission;
import com.hypersocket.tests.AbstractServerTest;

public class WithDelegatedPermissionTest extends AbstractServerTest {

	@BeforeClass
	public static void init() throws Exception {
		logOnNewUser(new String[] {
				AuthenticationPermission.LOGON.getResourceKey(),
				UserPermission.CREATE.getResourceKey() });
	}

	@AfterClass
	public static void clean() throws Exception {
		logoff();
	}

	@Test
	public void testCallPermissionList() throws Exception {
		doGet("/hypersocket/api/permissions/list");
	}

	@Test
	public void testUserCreationPermission() throws Exception {
		doGet("/hypersocket/api/permissions/permission/user.create/");
	}

}
