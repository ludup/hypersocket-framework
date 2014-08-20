package com.hypersocket.tests.menu;

import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hypersocket.json.JsonMenu;
import com.hypersocket.tests.AbstractServerTest;
 

public class WithAdminPermissionTest extends AbstractServerTest {

	@BeforeClass
	public static void init()throws Exception{
		logon("Default", "admin", "Password123?");
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
		assertTrue(menus.getMenus().length>0);
	}
	
	@Test
	public void testAccess() throws Exception{
		String json=doGet("/hypersocket/api/menus/tableActions/userActions");
		debugJSON(json);
		assertNotNull(json);
	}
	
	
	
	
}
