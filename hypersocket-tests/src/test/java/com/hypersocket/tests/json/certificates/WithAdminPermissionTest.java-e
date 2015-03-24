package com.hypersocket.tests.json.certificates;

import java.io.IOException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;



public class WithAdminPermissionTest extends AbstractPermissionsTest {


	@BeforeClass
	public static void LogOn() throws Exception {
		logon("System", "admin", "Password123?");

	}

	@AfterClass
	public static void logOff() throws JsonParseException,
			JsonMappingException, IOException {
		logoff();
	}

	
}
