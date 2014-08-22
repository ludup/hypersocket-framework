package com.hypersocket.tests.session;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hypersocket.json.JsonLogonResult;
import com.hypersocket.json.JsonSession;
import com.hypersocket.tests.AbstractServerTest;

public class UnauthorizedTests extends AbstractServerTest {

	private static JsonSession auxSession;

	@BeforeClass
	public static void LogOn() throws Exception {
		String logonJson = doPost("/hypersocket/api/logon",
				new BasicNameValuePair("username", "admin"),
				new BasicNameValuePair("password", "Password123?"));

		JsonLogonResult logon = getMapper().readValue(logonJson,
				JsonLogonResult.class);
		auxSession = logon.getSession();
		logoff();
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedSessionTouch() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/session/touch");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedSessionPeek() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/session/peek");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedSessionSwitchRealm()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/session/switchRealm/"
				+ auxSession.getCurrentRealm().getId());
	}

	@Test
	public void tryUnauthorizedSessionswitchLanguage()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/session/switchLanguage/en");
	}
}
