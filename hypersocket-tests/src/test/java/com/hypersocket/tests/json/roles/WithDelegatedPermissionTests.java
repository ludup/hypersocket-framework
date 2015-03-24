package com.hypersocket.tests.json.roles;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.hypersocket.auth.AuthenticationPermission;
import com.hypersocket.realm.RolePermission;

public class WithDelegatedPermissionTests extends AbstractPermissionsTest {

	@BeforeClass
	public static void logOn() throws Exception {

		logOnNewUser(new String[] {
				AuthenticationPermission.LOGON.getResourceKey(),
				RolePermission.CREATE.getResourceKey(),
				RolePermission.READ.getResourceKey(),
				RolePermission.UPDATE.getResourceKey(),
				RolePermission.DELETE.getResourceKey() });
	}

	@AfterClass
	public static void logOff() throws JsonParseException,
			JsonMappingException, IOException {
		logoff();
	}
}
