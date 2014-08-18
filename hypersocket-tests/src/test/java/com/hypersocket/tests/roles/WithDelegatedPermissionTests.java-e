package com.hypersocket.tests.roles;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.Assert;

import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.json.JsonRoleResourceStatus;
import com.hypersocket.permissions.json.RoleUpdate;
import com.hypersocket.tests.AbstractServerTest;

public class WithDelegatedPermissionTests extends AbstractServerTest {

	@BeforeClass
	public static void logOn() throws Exception {
		logon("Default", "admin", "Password123?");
		JsonResourceStatus jsonCreateUser = createUser("Default", "user",
				"user", false);
		changePassword("user", jsonCreateUser);
		Long[] permissions = { getPermissionId("permission.logon"),
				getPermissionId("role.create"), getPermissionId("role.read"),
				getPermissionId("role.update"), getPermissionId("role.delete") };
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

	@Test
	public void tryWithDelegatedPermissionRoleId()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/roles/role/" + getSystemAdminRole().getId());
	}

	@Test
	public void tryWithDelegatedPermissionRoleByName()
			throws ClientProtocolException, IOException {

		doGet("/hypersocket/api/roles/byName/System%20Administrator");
	}

	@Test
	public void tryWithDelegatedPermissionRoleTemplate()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/roles/template");
	}

	@Test
	public void tryWithDelegatedPermissionRoleList()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/roles/list");
	}

	@Test
	public void tryWithDelegatedPermissionRoleTable()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/roles/table");
	}

	@Test
	public void tryWithDelegatedPermissionCreateRolePost() throws Exception {
		RoleUpdate role = new RoleUpdate();
		role.setName("rolename");
		role.setPermissions(new Long[0]);
		Long[] permissions = { getPermissionId("permission.logon") };

		role.setPermissions(permissions);
		role.setUsers(new Long[0]);
		role.setGroups(new Long[0]);

		JsonResourceStatus json = getMapper().readValue(
				doPostJson("/hypersocket/api/roles/role", role),
				JsonResourceStatus.class);
		Assert.notNull(json.getResource().getId());
	}

	@Test
	public void tryWithDelegatedPermissionDeleteRoleId()
			throws ClientProtocolException, IOException {
		doDelete("/hypersocket/api/roles/role/" + getSystemAdminRole().getId());
	}
}
