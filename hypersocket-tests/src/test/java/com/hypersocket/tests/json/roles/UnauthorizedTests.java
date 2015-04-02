package com.hypersocket.tests.json.roles;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.hypersocket.auth.AuthenticationPermission;
import com.hypersocket.permissions.json.RoleUpdate;
import com.hypersocket.tests.AbstractServerTest;

public class UnauthorizedTests extends AbstractServerTest {

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedRoleId() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/roles/role/" + getSystemAdminRole().getId());
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedRoleByName() throws ClientProtocolException,
			IOException {

		doGet("/hypersocket/api/roles/byName/System%20Administrator");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedRoleTemplate() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/roles/template");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedRoleList() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/roles/list");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedRoleTable() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/roles/table");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedRolePost() throws Exception {
		RoleUpdate role = new RoleUpdate();
		role.setName("newRole");
		role.setPermissions(new Long[0]);
		Long[] permissions = { getPermissionId(AuthenticationPermission.LOGON
				.getResourceKey()) };

		role.setPermissions(permissions);
		role.setUsers(new Long[0]);
		role.setGroups(new Long[0]);

		doPostJson("/hypersocket/api/roles/role", role);

	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedDeleteRoleId() throws ClientProtocolException,
			IOException {
		doDelete("/hypersocket/api/roles/role/" + getSystemAdminRole().getId());
	}

}
