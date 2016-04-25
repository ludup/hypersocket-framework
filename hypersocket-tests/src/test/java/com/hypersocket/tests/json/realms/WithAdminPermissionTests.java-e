package com.hypersocket.tests.json.realms;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;

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
