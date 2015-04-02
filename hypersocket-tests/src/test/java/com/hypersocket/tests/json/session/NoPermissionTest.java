package com.hypersocket.tests.json.session;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.hypersocket.auth.AuthenticationPermission;
import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.tests.AbstractServerTest;

public class NoPermissionTest extends AbstractServerTest {
	@BeforeClass
	public static void logOn() throws Exception {

		logOnNewUser(new String[] { AuthenticationPermission.LOGON
				.getResourceKey() });
	}

	@Test(expected = ClientProtocolException.class)
	public void trySessionSwitchRealm() throws Exception {
		JsonResourceStatus json = createRealm("newRealm");
		doGet("/hypersocket/api/session/switchRealm/"
				+ json.getResource().getId());
	}

	@AfterClass
	public static void logOff() throws JsonParseException,
			JsonMappingException, IOException {
		logoff();
	}
}
