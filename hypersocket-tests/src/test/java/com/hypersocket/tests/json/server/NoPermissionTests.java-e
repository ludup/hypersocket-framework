package com.hypersocket.tests.json.server;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.hypersocket.auth.AuthenticationPermission;
import com.hypersocket.tests.AbstractServerTest;

public class NoPermissionTests extends AbstractServerTest {

	@BeforeClass
	public static void logOn() throws Exception {

		logOnNewUser(new String[] { AuthenticationPermission.LOGON
				.getResourceKey() });
	}

	@AfterClass
	public static void logOff() throws JsonParseException,
			JsonMappingException, IOException {
		logoff();
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionServerRestart() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/server/restart/" + adminId);
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionServerShutdown() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/server/shutdown/" + adminId);
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
