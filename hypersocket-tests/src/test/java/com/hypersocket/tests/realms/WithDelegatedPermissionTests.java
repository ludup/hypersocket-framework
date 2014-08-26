package com.hypersocket.tests.realms;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.hypersocket.auth.AuthenticationPermission;
import com.hypersocket.realm.RealmPermission;

public class WithDelegatedPermissionTests extends AbstractPermissionsTest {

	@BeforeClass
	public static void logOn() throws Exception {
		logOnNewUser(new String[] {
				AuthenticationPermission.LOGON.getResourceKey(),
				RealmPermission.CREATE.getResourceKey(),
				RealmPermission.READ.getResourceKey(),
				RealmPermission.UPDATE.getResourceKey(),
				RealmPermission.DELETE.getResourceKey() });
	}

	@AfterClass
	public static void logOff() throws JsonParseException,
			JsonMappingException, IOException {
		logoff();
	}
}
