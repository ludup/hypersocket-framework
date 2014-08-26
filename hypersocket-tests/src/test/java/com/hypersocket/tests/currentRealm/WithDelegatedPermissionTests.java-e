package com.hypersocket.tests.currentRealm;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.hypersocket.auth.AuthenticationPermission;
import com.hypersocket.realm.GroupPermission;
import com.hypersocket.realm.UserPermission;

public class WithDelegatedPermissionTests extends AbstractPermissionsTest {
	@BeforeClass
	public static void logOn() throws Exception {

		logOnNewUser(new String[] {
				AuthenticationPermission.LOGON.getResourceKey(),
				UserPermission.CREATE.getResourceKey(),
				UserPermission.READ.getResourceKey(),
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
