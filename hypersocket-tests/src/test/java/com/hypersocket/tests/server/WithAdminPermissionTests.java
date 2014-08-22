package com.hypersocket.tests.server;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

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
