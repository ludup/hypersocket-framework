package com.hypersocket.tests.json.currentRealm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.springframework.util.Assert;

import com.hypersocket.json.JsonResourceList;
import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.properties.json.PropertyItem;
import com.hypersocket.realm.json.CredentialsUpdate;
import com.hypersocket.realm.json.GroupUpdate;
import com.hypersocket.realm.json.UserUpdate;
import com.hypersocket.tests.AbstractServerTest;

public class AbstractPermissionsTest extends AbstractServerTest {

	@Test
	public void tryCurrentRealmGroupList() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/currentRealm/groups/list");
	}

	@Test
	public void tryCurrentRealmGroupsUser() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/currentRealm/groups/user/"
				+ getSession().getCurrentPrincipal().getId());
	}

	@Test
	public void tryCurrentRealmUsersList() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/currentRealm/users/list");
	}

	@Test
	public void tryCurrentRealmUsersTable() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/currentRealm/users/table");
	}

	@Test
	public void tryCurrentRealmGroupsTable() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/currentRealm/groups/table");
	}

	@Test
	public void tryCurrentRealmUsersGroup() throws ClientProtocolException,
			IOException {

		JsonResourceList json = getMapper().readValue(
				doGet("/hypersocket/api/currentRealm/groups/list"),
				JsonResourceList.class);

		doGet("/hypersocket/api/currentRealm/users/group/"
				+ json.getResources()[0].getId());
	}

	@Test
	public void tryCurrentRealmUserTemplate() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/currentRealm/user/template/local");
	}

	@Test
	public void tryCurrentRealmGroupTemplate() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/currentRealm/group/template/local");
	}

	@Test
	public void tryCurrentRealmUserProperties() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/currentRealm/user/properties/"
				+ getSession().getCurrentPrincipal().getId());
	}

	@Test
	public void tryCurrentRealmUserProfile() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/currentRealm/user/profile");
	}

	@Test
	public void tryCurrentRealmUserProfilePost() throws Exception {

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
	public void tryCurrentRealmIdGroup() throws ClientProtocolException,
			IOException {
		JsonResourceList json = getMapper().readValue(
				doGet("/hypersocket/api/currentRealm/groups/list"),
				JsonResourceList.class);

		doGet("/hypersocket/api/currentRealm/group/"
				+ json.getResources()[0].getId());
	}

	@Test
	public void tryCurrentRealmUserId() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/currentRealm/user/"
				+ getSession().getCurrentPrincipal().getId());
	}

	@Test
	public void tryCurrentRealmGroupPost() throws Exception {

		GroupUpdate group = new GroupUpdate();
		group.setName("newGroup");
		Long[] users = { getSession().getCurrentPrincipal().getId() };
		group.setUsers(users);

		JsonResourceStatus json = getMapper().readValue(
				doPostJson("/hypersocket/api/currentRealm/group", group),
				JsonResourceStatus.class);
		assertTrue(json.isSuccess());
		Assert.notNull(json.getResource().getId());
		assertEquals("newGroup", json.getResource().getName());
	}

	@Test
	public void tryCurrentRealmGroupDelete() throws Exception {

		JsonResourceStatus jsonGroup = createGroup("groupName");

		doDelete("/hypersocket/api/currentRealm/group/"
				+ jsonGroup.getResource().getId());
	}

	@Test
	public void tryCurrentRealmDeleteUser() throws Exception {
		JsonResourceStatus json = createUser(getSession().getCurrentRealm()
				.getName(), "userName", "password", false);

		doDelete("/hypersocket/api/currentRealm/user/"
				+ json.getResource().getId());
	}

	@Test
	public void tryCurrentRealmUserPost() throws Exception {

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
	public void tryCurrentRealmCredentialsPost()
			throws ClientProtocolException, IOException, IllegalStateException,
			URISyntaxException {
		CredentialsUpdate credentialsUpdate = new CredentialsUpdate();

		credentialsUpdate.setForceChange(false);
		credentialsUpdate.setPassword("newPass");

		credentialsUpdate.setPrincipalId(getSession().getCurrentPrincipal().getId());

		JsonResourceStatus json = getMapper().readValue(
				doPostJson("/hypersocket/api/currentRealm/user/credentials",
						credentialsUpdate), JsonResourceStatus.class);
		assertTrue(json.isSuccess());

	}
}
