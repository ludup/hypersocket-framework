package com.hypersocket.tests.json.session;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.tests.AbstractServerTest;

public class AbstractPermissionsTest extends AbstractServerTest {

	@Test
	public void trySessionTouch() throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/session/touch");
	}

	@Test
	public void trySessionPeek() throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/session/peek");
	}

	@Test
	public void trySessionSwitchRealm() throws Exception {
		JsonResourceStatus json = createRealm("newRealm");
		doGet("/hypersocket/api/session/switchRealm/"
				+ json.getResource().getId());
	}

	@Test
	public void trySessionswitchLanguage() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/session/switchLanguage/en");
	}
}
