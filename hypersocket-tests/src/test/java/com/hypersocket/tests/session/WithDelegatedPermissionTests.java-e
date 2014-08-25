package com.hypersocket.tests.session;

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

public class WithDelegatedPermissionTests extends AbstractServerTest {

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

	@Test
	public void tryWithDelegatedPermissionSessionTouch()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/session/touch");
	}

	@Test
	public void tryWithDelegatedPermissionSessionPeek()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/session/peek");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryWithDelegatedPermissionSessionSwitchRealm() throws Exception {
		doGet("/hypersocket/api/session/switchRealm/"
				+ getSession().getCurrentRealm().getId());
	}

	@Test
	public void tryWithDelegatedPermissionSessionswitchLanguage()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/session/switchLanguage/en");
	}
}
