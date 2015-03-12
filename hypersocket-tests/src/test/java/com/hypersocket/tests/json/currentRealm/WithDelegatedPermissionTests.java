package com.hypersocket.tests.json.currentRealm;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.hypersocket.auth.AuthenticationPermission;
import com.hypersocket.realm.GroupPermission;
import com.hypersocket.realm.ProfilePermission;
import com.hypersocket.realm.UserPermission;

public class WithDelegatedPermissionTests extends AbstractPermissionsTest {
	@BeforeClass
	public static void logOn() throws Exception {

		logOnNewUser(new String[] {
				AuthenticationPermission.LOGON.getResourceKey(),
				UserPermission.CREATE.getResourceKey(),
				UserPermission.READ.getResourceKey(),
				ProfilePermission.READ.getResourceKey(),
				UserPermission.UPDATE.getResourceKey(),
				UserPermission.DELETE.getResourceKey(),
				GroupPermission.CREATE.getResourceKey(),
				GroupPermission.READ.getResourceKey(),
				GroupPermission.UPDATE.getResourceKey(),
				GroupPermission.DELETE.getResourceKey() });
	}

	@AfterClass
	public static void logOff() throws JsonParseException,
			JsonMappingException, IOException {
		logoff();
	}
}
