package com.hypersocket.tests.json.menu;

import org.junit.AfterClass;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.hypersocket.json.JsonMenu;
import com.hypersocket.json.JsonUserResources;
import com.hypersocket.tests.AbstractServerTest;
 

public class WithAdminPermissionTest extends AbstractServerTest {

	@BeforeClass
	public static void init()throws Exception{
		logon("System", "admin", "Password123?");
	}
	
	@AfterClass
	public static void destroy() throws Exception{
		logoff();
	}
	
	@Test
	public void testMenuCall() throws Exception{
		String json=doGet("/hypersocket/api/menus");
		debugJSON(json);
		JsonMenu menus=getMapper().readValue(json,JsonMenu.class);
		JsonMenu personalMenu=menus.getMenus()[0];
		assertTrue(personalMenu.getId().equals("personal"));
		JsonMenu systemMenu=menus.getMenus()[1];
		assertTrue(systemMenu.getId().equals("system"));
	}
	
	@Test
	public void testAccess() throws Exception{
		String json=doGet("/hypersocket/api/menus/tableActions/userActions");
		debugJSON(json);
		JsonUserResources resources=getMapper().readValue(json,JsonUserResources.class); 
		assertTrue(resources.getResources()[0].getResourceKey().equals("setPassword"));
	}
	
	
	
	
}
