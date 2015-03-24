package com.hypersocket.tests.json.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.hypersocket.json.JsonConfiguration;
import com.hypersocket.json.JsonConfigurationTemplate;

public class WithDelegatedPermissionTest extends AbstractConfigurationTest {

	@Test
	public void testGetConfiguration() throws Exception {
		String json = doGet("/hypersocket/api/configuration");
		assertNotNull(json);
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
		assertNotNull(json);
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
}
