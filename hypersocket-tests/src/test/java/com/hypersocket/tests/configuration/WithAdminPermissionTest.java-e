package com.hypersocket.tests.configuration;

import java.io.IOException;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.AfterClass;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.hypersocket.tests.AbstractServerTest;

public class WithAdminPermissionTest extends AbstractServerTest {

	@BeforeClass
	public static void LogOn() throws Exception {
		logon("Default", "admin", "Password123?");
	}
	
	@AfterClass
	public static void logOff() throws JsonParseException,
			JsonMappingException, IOException {
		logoff();
	}
	
	@Test
	public void testGetConfiguration() throws Exception{
		String json=doGet("/hypersocket/api/configuration");
		assertNotNull(json);
	}
	
	@Test
	public void testGetSystemConfiguration() throws Exception{
		String json=doGet("/hypersocket/api/configuration/system");
		assertNotNull(json);
	}
	
	@Test
	public void testSystemGroupConfiguration() throws Exception{
		String json=doGet("/hypersocket/api/configuration/system/extensions");
		assertNotNull(json);
	}
	
	@Test
	public void testSystemRealmConfiguration() throws Exception{
		String json=doGet("/hypersocket/api/configuration/realm/system");
		debugJSON(json);
		assertNotNull(json);
	}
	
	
	
	
	
}
