package com.hypersocket.tests.menu;

import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hypersocket.json.JsonMenu;
import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.json.JsonRoleResourceStatus;
import com.hypersocket.tests.AbstractServerTest;

public class WithDelegatedPermissionTests extends AbstractServerTest {

	@BeforeClass
	public static void logOn() throws Exception {
		logon("Default", "admin", "Password123?");
		JsonResourceStatus jsonCreateUser = createUser("Default", "user",
				"user", false);
		changePassword("user", jsonCreateUser);
		Long[] permissions = { getPermissionId("permission.logon"),
				getPermissionId("user.create"), getPermissionId("user.read"),
				getPermissionId("user.update"), getPermissionId("user.delete") };
		JsonRoleResourceStatus jsonCreateRole = createRole("newrole",
				permissions);
		addUserToRole(jsonCreateRole.getResource(), jsonCreateUser);
		logoff();
		logon("Default", "user", "user");
	}
	
	@Test
	public void testAccessMenuAvailability() throws Exception{
		String json=doGet("/hypersocket/api/menus");
		debugJSON(json);
		JsonMenu menus=getMapper().readValue(json,JsonMenu.class);
		assertNotNull(menus);
		JsonMenu SystemMenu=menus.getMenus()[0];
		assertEquals(SystemMenu.getId(),"system");
		assertEquals(SystemMenu.getMenus()[0].getId(),"accessControl");
	}
	
}
