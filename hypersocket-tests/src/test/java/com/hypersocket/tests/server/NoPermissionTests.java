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
import com.hypersocket.tests.AbstractServerTest;

public class NoPermissionTests extends AbstractServerTest {

	@BeforeClass
	public static void logOn() throws Exception {

		logon("Default", "admin", "Password123?");
		JsonResourceStatus jsonCreateUser = createUser("Default", "user",
				"user", false);
		changePassword("user", jsonCreateUser);
		Long[] permissions = { getPermissionId(AuthenticationPermission.LOGON
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

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionServerRestart() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/server/restart/60");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionServerShutdown() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/server/shutdown/60");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionServerSslProtocols()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/server/sslProtocols");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionServerSslCiphers()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/server/sslCiphers");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionServerNetworkInterfaces()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/server/networkInterfaces");

	}
}
