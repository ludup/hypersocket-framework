package com.hypersocket.tests.json.permissions;

import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.json.JsonPermission;
import com.hypersocket.json.JsonPermissionList;
import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.tests.AbstractServerTest;

public class WithAdminPermissionTest extends AbstractServerTest {

	static ObjectMapper mapper = new ObjectMapper();
	static JsonPermissionList allPermisssions;
	
	@BeforeClass
	public static void init()throws Exception{
		logon("System", "admin", "Password123?");
		String permissionJson = doGet("/hypersocket/api/permissions/list");
		allPermisssions = mapper.readValue(permissionJson,JsonPermissionList.class);
	}
	
	@AfterClass
	public static void destroy() throws Exception{
		logoff();
	}
	
	
	
	@Test   //Test whether administrator has all the permission defined in the system 
	public void testAllPermissions() throws Exception{
		for(JsonPermission perm:allPermisssions.getPermissions()){
			String permissionJson =doGet("hypersocket/api/permissions/permission/"+perm.getResourceKey()+"/");
			JsonResourceStatus status = mapper.readValue(permissionJson,JsonResourceStatus.class);
			assertTrue(status.isSuccess());
		}
	}
	
	@Test
	public void testGetAllRoleList() throws Exception{
		doGet("/hypersocket/api/permissions/list");
	}
	
	
	
}


