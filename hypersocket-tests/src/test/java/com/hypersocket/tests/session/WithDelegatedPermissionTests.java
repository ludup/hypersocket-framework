package com.hypersocket.tests.session;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.hypersocket.auth.AuthenticationPermission;
import com.hypersocket.permissions.SystemPermission;

public class WithDelegatedPermissionTests extends AbstractPermissionsTest {

	@BeforeClass
	public static void logOn() throws Exception {

		logOnNewUser(new String[] {
				AuthenticationPermission.LOGON.getResourceKey(),
				SystemPermission.SYSTEM_ADMINISTRATION.getResourceKey() });
	}

	@AfterClass
	public static void logOff() throws JsonParseException,
			JsonMappingException, IOException {
		logoff();
	}
}
