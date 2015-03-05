package com.hypersocket.tests.json.session;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.hypersocket.tests.AbstractServerTest;

public class UnauthorizedTests extends AbstractServerTest {

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
				+ getAuxSession().getCurrentRealm().getId());
	}

	@Test
	public void tryUnauthorizedSessionswitchLanguage()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/session/switchLanguage/en");
	}
}
