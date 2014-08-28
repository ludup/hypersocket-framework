package com.hypersocket.tests.configuration;

import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hypersocket.auth.AuthenticationPermission;
import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.json.JsonRoleResourceStatus;
import com.hypersocket.tests.AbstractServerTest;

public class NoPermissionTest extends AbstractConfigurationTest {

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
