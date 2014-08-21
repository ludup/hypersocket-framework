package com.hypersocket.tests.configuration;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.json.JsonRoleResourceStatus;
import com.hypersocket.tests.AbstractServerTest;

public class NoPermissionTest extends AbstractServerTest {

	@BeforeClass
	public static void logOn() throws Exception {

		logon("Default", "admin", "Password123?");
		JsonResourceStatus jsonCreateUser = createUser("Default", "user",
				"user", false);
		changePassword("user", jsonCreateUser);
		Long[] permissions = { getPermissionId("permission.logon") };
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
	
	@Test(expected=ClientProtocolException.class)
	public void testgetConfiguationWithoutLogon() throws Exception{
		String json=doGet("/hypersocket/api/configuration");
	}
	
	@Test(expected=ClientProtocolException.class)
	public void testGetSystemConfigurationWithoutLogon() throws Exception{
		doGet("/hypersocket/api/configuration/system");
	}
	
	@Test(expected=ClientProtocolException.class)
	public void testSystemGroupConfigurationWithoutLogon() throws Exception{
		String json=doGet("/hypersocket/api/configuration/system/extensions");
		
	}
	
	@Test(expected=ClientProtocolException.class)
	public void testSystemRealmConfigurationWithoutLogon() throws Exception{
		String json=doGet("/hypersocket/api/configuration/realm/system");
		
	}
}
