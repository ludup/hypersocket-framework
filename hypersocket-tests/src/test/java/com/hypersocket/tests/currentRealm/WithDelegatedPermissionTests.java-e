package com.hypersocket.tests.currentRealm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.Assert;

import com.hypersocket.auth.AuthenticationPermission;
import com.hypersocket.json.JsonResourceList;
import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.properties.json.PropertyItem;
import com.hypersocket.realm.GroupPermission;
import com.hypersocket.realm.UserPermission;
import com.hypersocket.realm.json.CredentialsUpdate;
import com.hypersocket.realm.json.GroupUpdate;
import com.hypersocket.realm.json.UserUpdate;
import com.hypersocket.tests.AbstractServerTest;

public class WithDelegatedPermissionTests extends AbstractServerTest {
	@BeforeClass
	public static void logOn() throws Exception {

		logOnNewUser(new String[] {
				AuthenticationPermission.LOGON.getResourceKey(),
				UserPermission.CREATE.getResourceKey(),
				UserPermission.READ.getResourceKey(),
				UserPermission.UPDATE.getResourceKey(),
				UserPermission.DELETE.getResourceKey(),
				GroupPermission.CREATE.getResourceKey(),
				GroupPermission.READ.getResourceKey(),
				GroupPermission.UPDATE.getResourceKey(),
				GroupPermission.DELETE.getResourceKey() });
	}

	@AfterClass
	public static void logOff() throws JsonParseException,
			JsonMappingException, IOException {
		logoff();
	}

	@Test
	public void tryWithAdminPermissionCurrentRealmGroupList()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/groups/list");
	}

	@Test
	public void tryWithAdminPermissionCurrentRealmGroupsUser()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/groups/user/"
				+ getSession().getPrincipal().getId());
	}

	@Test
	public void tryWithAdminPermissionCurrentRealmUsersList()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/users/list");
	}

	@Test
	public void tryWithAdminPermissionCurrentRealmUsersTable()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/users/table");
	}

	@Test
	public void tryWithAdminPermissionCurrentRealmGroupsTable()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/groups/table");
	}

	@Test
	public void tryWithAdminPermissionCurrentRealmUsersGroup()
			throws ClientProtocolException, IOException {

		JsonResourceList json = getMapper().readValue(
				doGet("/hypersocket/api/currentRealm/groups/list"),
				JsonResourceList.class);

		doGet("/hypersocket/api/currentRealm/users/group/"
				+ json.getResources()[0].getId());
	}

	@Test
	public void tryWithAdminPermissionCurrentRealmUserTemplate()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/user/template/local");
	}

	@Test
	public void tryWithAdminPermissionCurrentRealmGroupTemplate()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/group/template/local");
	}

	@Test
	public void tryWithAdminPermissionCurrentRealmUserProperties()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/user/properties/"
				+ getSession().getPrincipal().getId());
	}

	@Test
	public void tryWithAdminPermissionCurrentRealmUserProfile()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/user/profile");
	}

	@Test
	public void tryWithAdminPermissionCurrentRealmUserProfilePost()
			throws Exception {

		PropertyItem item = new PropertyItem();
		item.setId("user.phone");
		item.setValue("666");
		PropertyItem[] items = { item };
		JsonResourceStatus json = getMapper()
				.readValue(
						doPostJson(
								"/hypersocket/api/currentRealm/user/profile",
								items), JsonResourceStatus.class);
		assertTrue(json.isSuccess());
	}

	@Test
	public void tryWithAdminPermissionCurrentRealmIdGroup()
			throws ClientProtocolException, IOException {
		JsonResourceList json = getMapper().readValue(
				doGet("/hypersocket/api/currentRealm/groups/list"),
				JsonResourceList.class);

		doGet("/hypersocket/api/currentRealm/group/"
				+ json.getResources()[0].getId());
	}

	@Test
	public void tryWithAdminPermissionCurrentRealmUserId()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/user/"
				+ getSession().getPrincipal().getId());
	}

	@Test
	public void tryWithAdminPermissionCurrentRealmGroupPost() throws Exception {

		GroupUpdate group = new GroupUpdate();
		group.setName("newGroup");
		Long[] users = { getSession().getPrincipal().getId() };
		group.setUsers(users);
		JsonResourceStatus json = getMapper().readValue(
				doPostJson("/hypersocket/api/currentRealm/group", group),
				JsonResourceStatus.class);
		assertTrue(json.isSuccess());
		Assert.notNull(json.getResource().getId());
		assertEquals("newGroup", json.getResource().getName());
	}

	@Test
	public void tryWithAdminPermissionCurrentRealmGroupDelete()
			throws Exception {

		JsonResourceStatus jsonGroup = createGroup("groupName");

		doDelete("/hypersocket/api/currentRealm/group/"
				+ jsonGroup.getResource().getId());
	}

	@Test
	public void tryWithAdminPermissionCurrentRealmDeleteUser() throws Exception {
		JsonResourceStatus json = createUser(getSession().getCurrentRealm()
				.getName(), "userName", "password", false);

		doDelete("/hypersocket/api/currentRealm/user/"
				+ json.getResource().getId());
	}

	@Test
	public void tryWithAdminPermissionCurrentRealmUserPost() throws Exception {

		UserUpdate user = new UserUpdate();
		user.setName("newUser");
		user.setPassword("newUserPass");
		user.setProperties(new PropertyItem[0]);
		user.setGroups(new Long[0]);

		JsonResourceStatus json = getMapper().readValue(
				doPostJson("/hypersocket/api/currentRealm/user", user),
				JsonResourceStatus.class);
		assertTrue(json.isSuccess());
		Assert.notNull(json.getResource().getId());
		assertEquals("newUser", json.getResource().getName());
	}

	@Test
	public void tryWithAdminPermissionCurrentRealmCredentialsPost()
			throws ClientProtocolException, IOException, IllegalStateException,
			URISyntaxException {
		CredentialsUpdate credentialsUpdate = new CredentialsUpdate();

		credentialsUpdate.setForceChange(false);
		credentialsUpdate.setPassword("newPass");

		credentialsUpdate.setPrincipalId(getSession().getPrincipal().getId());

		JsonResourceStatus json = getMapper().readValue(
				doPostJson("/hypersocket/api/currentRealm/user/credentials",
						credentialsUpdate), JsonResourceStatus.class);
		assertTrue(json.isSuccess());

	}
}
