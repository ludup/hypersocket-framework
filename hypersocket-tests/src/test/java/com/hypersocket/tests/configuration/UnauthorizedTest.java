package com.hypersocket.tests.configuration;

import org.apache.http.client.ClientProtocolException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.*;

import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.json.JsonRoleResourceStatus;
import com.hypersocket.tests.AbstractServerTest;

public class UnauthorizedTest extends AbstractServerTest {
		
	@Test(expected=ClientProtocolException.class)
	public void testgetConfiguationWithoutLogon() throws Exception{
		String json=doGet("/hypersocket/api/configuration");
	}
	
	@Test(expected=ClientProtocolException.class)
	public void testGetSystemConfigurationWithoutLogon() throws Exception{
		doGet("/hypersocket/api/configuration/system");
	}
	
	@Test(expected=ClientProtocolException.class)
	public void testSystemGroupConfigurationWithoutLogon() throws Exception{
		String json=doGet("/hypersocket/api/configuration/system/extensions");
		
	}
	
	@Test(expected=ClientProtocolException.class)
	public void testSystemRealmConfigurationWithoutLogon() throws Exception{
		String json=doGet("/hypersocket/api/configuration/realm/system");
		
	}
	
	
	
	
}
