package com.hypersocket.tests.session;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.tests.AbstractServerTest;

public class WithAdminPermissionTests extends AbstractServerTest {

	@BeforeClass
	public static void logOn() throws Exception {
		logon("Default", "admin", "Password123?");
	}

	@AfterClass
	public static void logOff() throws JsonParseException,
			JsonMappingException, IOException {
		logoff();
	}

	@Test
	public void tryWithAdminPermissionSessionTouch()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/session/touch");
	}

	@Test
	public void tryWithAdminPermissionSessionPeek()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/session/peek");
	}

	@Test
	public void tryWithAdminPermissionSessionSwitchRealm() throws Exception {
		JsonResourceStatus json = createRealm("newRealm");
		doGet("/hypersocket/api/session/switchRealm/"
				+ json.getResource().getId());
	}

	@Test
	public void tryWithAdminPermissionSessionswitchLanguage()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/session/switchLanguage/en");
	}

}
