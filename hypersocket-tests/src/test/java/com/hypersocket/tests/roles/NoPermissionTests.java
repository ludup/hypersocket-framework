package com.hypersocket.tests.roles;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.json.JsonRoleResourceStatus;
import com.hypersocket.permissions.json.RoleUpdate;
import com.hypersocket.tests.AbstractServerTest;

public class NoPermissionTests extends AbstractServerTest {

	@BeforeClass
	public static void logOn() throws Exception {

		logon("Default", "admin", "Password123?");
		JsonResourceStatus jsonCreateUser = createUser("Default", "user",
				"user", false);
		changePassword("user", jsonCreateUser);
		Long[] permissions = { getPermissionId("permission.logon") };
		JsonRoleResourceStatus jsonCreateRole = createRole("newrole",
				permissions);
		addUserToRole(jsonCreateRole.getResource(), jsonCreateUser);
		logoff();
		logon("Default", "user", "user");
	}

	@AfterClass
	public static void logOff() throws JsonParseException,
			JsonMappingException, IOException {
		logoff();
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionRoleId() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/roles/role/" + getSystemAdminRole().getId());
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionRoleByName() throws ClientProtocolException,
			IOException {

		doGet("/hypersocket/api/roles/byName/System%20Administrator");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionRoleTemplate() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/roles/template");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionRoleList() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/roles/list");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionRoleTable() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/roles/table");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedNetworkInterfaces() throws Exception {
		RoleUpdate role = new RoleUpdate();
		role.setName("rolename");
		role.setPermissions(new Long[0]);
		Long[] permissions = { getPermissionId("permission.logon") };

		role.setPermissions(permissions);
		role.setUsers(new Long[0]);
		role.setGroups(new Long[0]);

		doPostJson("/hypersocket/api/roles/role", role);

	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionDeleteRoleId() throws ClientProtocolException,
			IOException {
		doDelete("/hypersocket/api/roles/role/" + getSystemAdminRole().getId());
	}
}
