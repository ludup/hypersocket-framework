package com.hypersocket.tests.server;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.hypersocket.tests.AbstractServerTest;

public class AbstractPermissionsTest extends AbstractServerTest {

	@Test
	public void tryServerRestart() throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/server/restart/5");
	}

	@Test
	public void tryServerShutdown() throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/server/shutdown/5");
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
