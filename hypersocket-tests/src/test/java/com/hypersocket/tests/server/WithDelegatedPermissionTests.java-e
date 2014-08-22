package com.hypersocket.tests.server;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hypersocket.auth.AuthenticationPermission;
import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.json.JsonRoleResourceStatus;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.tests.AbstractServerTest;

public class WithDelegatedPermissionTests extends AbstractServerTest {

	@BeforeClass
	public static void logOn() throws Exception {
		logon("Default", "admin", "Password123?");
		JsonResourceStatus jsonCreateUser = createUser("Default", "user",
				"user", false);
		changePassword("user", jsonCreateUser);
		Long[] permissions = {
				getPermissionId(AuthenticationPermission.LOGON.getResourceKey()),
				getPermissionId(SystemPermission.SYSTEM_ADMINISTRATION
						.getResourceKey()) };
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
	public void tryWithAdminPermissionServerRestart()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/server/restart/5");
	}

	@Test
	public void tryWithAdminPermissionServerShutdown()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/server/shutdown/5");
	}

	@Test
	public void tryWithAdminPermissionServerSslProtocols()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/server/sslProtocols");
	}

	@Test
	public void tryWithAdminPermissionServerSslCiphers()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/server/sslCiphers");
	}

	@Test
	public void tryWithAdminPermissionServerNetworkInterfaces()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/server/networkInterfaces");

	}
}
