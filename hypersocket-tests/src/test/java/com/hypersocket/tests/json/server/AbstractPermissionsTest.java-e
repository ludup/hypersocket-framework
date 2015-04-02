package com.hypersocket.tests.json.server;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.hypersocket.tests.AbstractServerTest;

public class AbstractPermissionsTest extends AbstractServerTest {

	@Test
	public void tryServerRestart() throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/server/restart/" + adminId);
	}

	@Test
	public void tryServerShutdown() throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/server/shutdown/" + adminId);
	}

	@Test
	public void tryServerSslProtocols() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/server/sslProtocols");
	}

	@Test
	public void tryServerSslCiphers() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/server/sslCiphers");
	}

	@Test
	public void tryServerNetworkInterfaces() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/server/networkInterfaces");

	}
}
