package com.hypersocket.tests.json.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.hypersocket.json.JsonConfiguration;
import com.hypersocket.json.JsonConfigurationTemplate;
import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.properties.json.PropertyItem;
import com.hypersocket.tests.AbstractServerTest;
import com.hypersocket.tests.json.configuration.AbstractConfigurationTest.Resources;

public class WithAdminPermissionTest extends AbstractServerTest {

	@BeforeClass
	public static void init() throws Exception {
		logon("System", "admin", "Password123?");
	}

	@AfterClass
	public static void logOff() throws JsonParseException,
			JsonMappingException, IOException {
		logoff();
	}

	@Test
	public void testGetConfiguration() throws Exception {
		String json = doGet("/hypersocket/api/configuration");
		debugJSON(json);
		Resources resources = getMapper().readValue(json, Resources.class);
		// test AuthenticationService contains categorykey category.security
		JsonConfiguration authConfig = null;
		for (JsonConfiguration conf : resources.getResources()) {
			if (conf.getBundle().equals("AuthenticationService")) {
				authConfig = conf;
				break;
			}
		}
		assertNotNull(authConfig);
		assertEquals("category.security", authConfig.getCategoryKey());

	}

	@Test
	public void testGetSystemConfiguration() throws Exception {
		String json = doGet("/hypersocket/api/configuration/system");
		debugJSON(json);
		Resources resources = getMapper().readValue(json, Resources.class);
		// Test HypersocketServer bundle contains template for application name
		// with value Hypersocket
		JsonConfiguration serverConfig = null;
		for (JsonConfiguration conf : resources.getResources()) {
			if (conf.getBundle().equals("HypersocketServer")) {
				serverConfig = conf;
				break;
			}
		}
		assertNotNull(serverConfig);
		JsonConfigurationTemplate applicationTemp = null;
		for (JsonConfigurationTemplate template : serverConfig.getTemplates()) {
			if (template.getResourceKey().equals("application.name")) {
				applicationTemp = template;
			}
		}
		assertNotNull(applicationTemp);
		assertEquals("Hypersocket", applicationTemp.getValue());

	}

	@Test
	public void testSystemGroupConfiguration() throws Exception {
		String json = doGet("/hypersocket/api/configuration/system/extensions");
		assertNotNull(json);
		debugJSON(json);
	}

	@Test
	public void testSystemRealmConfiguration() throws Exception {
		String json = doGet("/hypersocket/api/configuration/realm/system");
		debugJSON(json);
		assertNotNull(json);
		Resources resources = getMapper().readValue(json, Resources.class);
		// Test to exist of I18NService
		JsonConfiguration I18NServiceConfig = null;
		for (JsonConfiguration conf : resources.getResources()) {
			if (conf.getBundle().equals("I18NService")) {
				I18NServiceConfig = conf;
				break;
			}
		}
		assertNotNull(I18NServiceConfig);
	}

	@Test
	public void testPostSystemRealm() throws Exception {
		PropertyItem[] properties = new PropertyItem[1];
		properties[0] = new PropertyItem();
		properties[0].setId("logon.banner");
		properties[0].setMessage(null);
		properties[0].setValue("new banner");
		JsonResourceStatus json = getMapper().readValue(
				doPostJson("/hypersocket/api/configuration/realm/system",
						properties), JsonResourceStatus.class);
		assertTrue(json.isSuccess());

	}

}
