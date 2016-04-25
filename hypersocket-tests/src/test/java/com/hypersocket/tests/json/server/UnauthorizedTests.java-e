package com.hypersocket.tests.json.server;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.hypersocket.tests.AbstractServerTest;

public class UnauthorizedTests extends AbstractServerTest {

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedServerRestart() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/server/restart/" + adminId);
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedServerShutdown() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/server/shutdown/" + adminId);
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedServerSslProtocols()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/server/sslProtocols");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedServerSslCiphers()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/server/sslCiphers");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedServerNetworkInterfaces()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/server/networkInterfaces");

	}

}
