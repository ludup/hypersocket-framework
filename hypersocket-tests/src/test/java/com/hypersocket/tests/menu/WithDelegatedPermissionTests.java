package com.hypersocket.tests.menu;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.hypersocket.json.JsonMenu;

public class WithDelegatedPermissionTests extends AbstractMenuTest {

	@Test
	public void testAccessMenuAvailability() throws Exception {
		String json = doGet("/hypersocket/api/menus");
		debugJSON(json);
		JsonMenu menus = getMapper().readValue(json, JsonMenu.class);
		assertNotNull(menus);
		JsonMenu systemMenu = null;
		for (JsonMenu menu : menus.getMenus()) {
			if (menu.getId().equals("system")) {
				systemMenu = menu;
				break;
			}
		}
		assertNotNull(systemMenu);
		JsonMenu accessMenu = null;
		for (JsonMenu menu : systemMenu.getMenus()) {
			if (menu.getId().equals("accessControl")) {
				accessMenu = menu;
				break;
			}
		}
		assertNotNull(accessMenu);
	}

	@Test
	public void testForUserCreation() throws Exception {
		String json = doGet("/hypersocket/api/menus");
		debugJSON(json);
		JsonMenu menus = getMapper().readValue(json, JsonMenu.class);
		assertNotNull(menus);
		JsonMenu systemMenu = null;
		for (JsonMenu menu : menus.getMenus()) {
			if (menu.getId().equals("system")) {
				systemMenu = menu;
				break;
			}
		}
		assertNotNull(systemMenu);
		JsonMenu accessMenu = null;
		for (JsonMenu menu : systemMenu.getMenus()) {
			if (menu.getId().equals("accessControl")) {
				accessMenu = menu;
				break;
			}
		}
		assertNotNull(accessMenu);
		JsonMenu userMenu = null;
		for (JsonMenu menu : accessMenu.getMenus()) {
			if (menu.getId().equals("users")) {
				userMenu = menu;
				break;
			}
		}
		assertNotNull(userMenu);
		assertTrue(userMenu.isCanCreate());

	}

}
