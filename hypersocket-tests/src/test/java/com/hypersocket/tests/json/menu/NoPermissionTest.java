package com.hypersocket.tests.json.menu;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.hypersocket.json.JsonMenu;

public class NoPermissionTest extends AbstractMenuTest {

	@Test
	public void testLicenseMenuNotAvailable() throws Exception {
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
		JsonMenu licenseMenu = null;
		for (JsonMenu menu : systemMenu.getMenus()) {
			if (menu.getId().equals("licenses")) {
				licenseMenu = menu;
				break;
			}
		}
		assertNull(licenseMenu);
	}

	@Test
	public void testResourcesMenuNotAvailable() throws Exception {
		String json = doGet("/hypersocket/api/menus");
		debugJSON(json);
		JsonMenu menus = getMapper().readValue(json, JsonMenu.class);
		JsonMenu resourceMenu = null;
		for (JsonMenu menu : menus.getMenus()) {
			if (menu.getId().equals("resources")) {
				resourceMenu = menu;
				break;
			}
		}
		assertNull(resourceMenu);
	}

	@Test
	public void testPersonalMenusAvailable() throws Exception {
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

	}

	@Test
	public void testProfileMenuNotAvailable() throws Exception{
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
		JsonMenu profileMenu = null;
		for (JsonMenu menu : personalMenu.getMenus()) {
			if (menu.getId().equals("profile")) {
				profileMenu = menu;
				break;
			}
		}
		assertNull(profileMenu);
	}

}
