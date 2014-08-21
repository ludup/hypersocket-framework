package com.hypersocket.tests.configuration;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.json.JsonRoleResourceStatus;
import com.hypersocket.tests.AbstractServerTest;

public class WithDelegatedPermissionTest extends AbstractServerTest {

	@BeforeClass
	public static void logOn() throws Exception {

		logon("Default", "admin", "Password123?");
		JsonResourceStatus jsonCreateUser = createUser("Default", "user",
				"user", false);
		changePassword("user", jsonCreateUser);
		Long[] permissions = { getPermissionId("permission.logon"),getPermissionId("system.permission") };
		JsonRoleResourceStatus jsonCreateRole = createRole("newrole",
				permissions);
		addUserToRole(jsonCreateRole.getResource(), jsonCreateUser);
		logoff();
		logon("Default", "user", "user");
	}

	@AfterClass
	public static void logOff() throws JsonParseException,
			JsonMappingException, IOException {
		logoff();
	}
	
	@Test
	public void testGetConfiguration() throws Exception{
		String json=doGet("/hypersocket/api/configuration");
		assertNotNull(json);
	}
	
	@Test
	public void testGetSystemConfiguration() throws Exception{
		String json=doGet("/hypersocket/api/configuration/system");
		assertNotNull(json);
	}
	
	@Test
	public void testSystemGroupConfiguration() throws Exception{
		String json=doGet("/hypersocket/api/configuration/system/extensions");
		assertNotNull(json);
	}
	
	@Test
	public void testSystemRealmConfiguration() throws Exception{
		String json=doGet("/hypersocket/api/configuration/realm/system");
		debugJSON(json);
		assertNotNull(json);
	}
}
