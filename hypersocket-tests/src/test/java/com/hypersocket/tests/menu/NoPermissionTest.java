package com.hypersocket.tests.menu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hypersocket.auth.AuthenticationPermission;
import com.hypersocket.json.JsonMenu;
import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.json.JsonRoleResourceStatus;
import com.hypersocket.tests.AbstractServerTest;

public class NoPermissionTest extends AbstractMenuTest {


	@Test
	public void testConfigurationMenuNotAvailable() throws Exception {
		String json = doGet("/hypersocket/api/menus");
		debugJSON(json);
		JsonMenu menus = getMapper().readValue(json, JsonMenu.class);
		JsonMenu systemMenu=null;
		for(JsonMenu menu:menus.getMenus()){
			if(menu.getId().equals("system")){
				systemMenu=menu;
				break;
			}
		}
		assertNotNull(systemMenu);
		JsonMenu configMenu=null;
		for(JsonMenu menu:systemMenu.getMenus()){
			if(menu.getId().equals("configuration")){
				configMenu=menu;
				break;
			}
		}
		assertNull(configMenu);
	}
	
	@Test
	public void testResourcesMenuNotAvailable() throws Exception{
		String json = doGet("/hypersocket/api/menus");
		debugJSON(json);
		JsonMenu menus = getMapper().readValue(json, JsonMenu.class);
		JsonMenu resourceMenu=null;
		for(JsonMenu menu:menus.getMenus()){
			if(menu.getId().equals("resources")){
				resourceMenu=menu;
				break;
			}
		}
		assertNull(resourceMenu);
	}
	
	@Test
	public void testPersonalMenusNotAvailable() throws Exception{
		String json = doGet("/hypersocket/api/menus");
		debugJSON(json);
		JsonMenu menus = getMapper().readValue(json, JsonMenu.class);
		JsonMenu personalMenu=null;
		for(JsonMenu menu:menus.getMenus()){
			if(menu.getId().equals("personal")){
				personalMenu=menu;
				break;
			}
		}
		assertNull(personalMenu);

	}

	
	@Test
	public void testGroupMenuNotAvailable() throws Exception{
		String json = doGet("/hypersocket/api/menus");
		debugJSON(json);
		JsonMenu menus = getMapper().readValue(json, JsonMenu.class);
		assertNotNull(menus);
		JsonMenu systemMenu=null;
		for(JsonMenu menu:menus.getMenus()){
			if(menu.getId().equals("system")){
				systemMenu=menu;
				break;
			}
		}
		assertNotNull(systemMenu);
		JsonMenu accessMenu=null;
		for(JsonMenu menu:systemMenu.getMenus()){
			if(menu.getId().equals("accessControl")){
				accessMenu=menu;
				break;
			}
		}
		assertNotNull(accessMenu);
		JsonMenu groupsMenu=null;
		for(JsonMenu menu:accessMenu.getMenus()){
			if(menu.getId().equals("groups")){
				groupsMenu=menu;
				break;
			}
		}
		assertNull(groupsMenu);
	}
	
}
