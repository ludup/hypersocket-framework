package com.hypersocket.tests.json.certificates;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.hypersocket.auth.AuthenticationPermission;
import com.hypersocket.permissions.SystemPermission;

public class WithDelegatedPermissionTest extends AbstractPermissionsTest {

	@BeforeClass
	public static void LogOn() throws Exception {
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
