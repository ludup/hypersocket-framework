package com.hypersocket.tests.json.configuration;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.hypersocket.tests.AbstractServerTest;

public class UnauthorizedTest extends AbstractServerTest {

	@Test(expected = ClientProtocolException.class)
	public void testgetConfiguationWithoutLogon() throws Exception {
		doGet("/hypersocket/api/configuration");
	}

	@Test(expected = ClientProtocolException.class)
	public void testGetSystemConfigurationWithoutLogon() throws Exception {
		doGet("/hypersocket/api/configuration/system");
	}

	@Test(expected = ClientProtocolException.class)
	public void testSystemGroupConfigurationWithoutLogon() throws Exception {
		doGet("/hypersocket/api/configuration/system/extensions");

	}

	@Test(expected = ClientProtocolException.class)
	public void testSystemRealmConfigurationWithoutLogon() throws Exception {
		doGet("/hypersocket/api/configuration/realm/system");

	}

}
