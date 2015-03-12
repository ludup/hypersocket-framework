package com.hypersocket.tests.json.menu;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import com.hypersocket.json.JsonMenu;

public class WithDelegatedPermissionTests extends AbstractMenuTest {
	
	@Test
	public void testCertificateMenuAvailable() throws Exception {
		String json = doGet("/hypersocket/api/menus");
		debugJSON(json);
		JsonMenu menus = getMapper().readValue(json, JsonMenu.class);
		JsonMenu systemMenu = null;
		for (JsonMenu menu : menus.getMenus()) {
			if (menu.getId().equals("system")) {
				systemMenu = menu;
				break;
			}
		}
		assertNotNull(systemMenu);
		JsonMenu configurationMenu = null;
		for (JsonMenu menu : systemMenu.getMenus()) {
			if (menu.getId().equals("configuration")) {
				configurationMenu = menu;
				break;
			}
		}
		assertNotNull(configurationMenu);
		JsonMenu certificatesMenu = null;
		for (JsonMenu menu : configurationMenu.getMenus()) {
			if (menu.getId().equals("certificates")) {
				certificatesMenu = menu;
				break;
			}
		}
		assertNotNull(certificatesMenu);
	}
	
	@Test
	public void testMyResourcesMenuAvailable() throws Exception {
		String json = doGet("/hypersocket/api/menus");
		debugJSON(json);
		JsonMenu menus = getMapper().readValue(json, JsonMenu.class);
		JsonMenu personalMenu = null;
		for (JsonMenu menu : menus.getMenus()) {
			if (menu.getId().equals("personal")) {
				personalMenu = menu;
				break;
			}
		}
		assertNotNull(personalMenu);
		JsonMenu resourceMenu = null;
		for (JsonMenu menu : personalMenu.getMenus()) {
			if (menu.getId().equals("myResources")) {
				resourceMenu = menu;
				break;
			}
		}
		assertNotNull(resourceMenu);
		
	}
		

	

}
