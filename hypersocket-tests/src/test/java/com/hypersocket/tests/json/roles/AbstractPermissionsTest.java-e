package com.hypersocket.tests.json.roles;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.springframework.util.Assert;

import com.hypersocket.auth.AuthenticationPermission;
import com.hypersocket.json.JsonRoleResourceStatus;
import com.hypersocket.permissions.json.RoleUpdate;
import com.hypersocket.tests.AbstractServerTest;

public class AbstractPermissionsTest extends AbstractServerTest {

	@Test
	public void tryRoleId() throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/roles/role/" + getSystemAdminRole().getId());
	}

	@Test
	public void tryRoleByName() throws ClientProtocolException, IOException {

		doGet("/hypersocket/api/roles/byName/System%20Administrator");
	}

	@Test
	public void tryRoleTemplate() throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/roles/template");
	}

	@Test
	public void tryRoleList() throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/roles/list");
	}

	@Test
	public void tryRoleTable() throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/roles/table");
	}

	@Test
	public void tryCreateRolePost() throws Exception {
		RoleUpdate role = new RoleUpdate();
		role.setName("newTestRole");
		role.setPermissions(new Long[0]);
		Long[] permissions = { getPermissionId(AuthenticationPermission.LOGON
				.getResourceKey()) };

		role.setPermissions(permissions);
		role.setUsers(new Long[0]);
		role.setGroups(new Long[0]);

		JsonRoleResourceStatus json = getMapper().readValue(
				doPostJson("/hypersocket/api/roles/role", role),
				JsonRoleResourceStatus.class);

		assertTrue(json.isSuccess());
		Assert.notNull(json.getResource().getId());
		assertEquals("newTestRole", json.getResource().getName());
	}

	@Test
	public void tryDeleteRoleId() throws Exception {
		JsonRoleResourceStatus jsonRole = createRole("roleName",
				new Long[] { getPermissionId(AuthenticationPermission.LOGON
						.getResourceKey()) });
		doDelete("/hypersocket/api/roles/role/"
				+ jsonRole.getResource().getId());
	}
}
