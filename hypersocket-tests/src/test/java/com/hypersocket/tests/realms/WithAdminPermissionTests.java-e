package com.hypersocket.tests.realms;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class WithAdminPermissionTests extends AbstractPermissionsTest {

	@BeforeClass
	public static void logOn() throws Exception {
		logon("System", "admin", "Password123?");
	}

	@AfterClass
	public static void logOff() throws JsonParseException,
			JsonMappingException, IOException {
		logoff();
	}
}
