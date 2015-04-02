package com.hypersocket.tests.json.configuration;

import org.apache.http.client.ClientProtocolException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hypersocket.auth.AuthenticationPermission;
import com.hypersocket.tests.AbstractServerTest;

public class NoPermissionTest extends AbstractServerTest {

	@BeforeClass
	public static void init() throws Exception {
		logOnNewUser(new String[] { AuthenticationPermission.LOGON
				.getResourceKey() });
	}

	@AfterClass
	public static void clean() throws Exception {
		logoff();
	}

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
