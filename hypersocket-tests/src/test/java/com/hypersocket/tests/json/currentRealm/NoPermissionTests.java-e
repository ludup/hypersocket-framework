package com.hypersocket.tests.json.currentRealm;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.hypersocket.auth.AuthenticationPermission;
import com.hypersocket.json.JsonResourceList;
import com.hypersocket.properties.json.PropertyItem;
import com.hypersocket.realm.json.CredentialsUpdate;
import com.hypersocket.realm.json.GroupUpdate;
import com.hypersocket.realm.json.UserUpdate;
import com.hypersocket.tests.AbstractServerTest;

public class NoPermissionTests extends AbstractServerTest {

	@BeforeClass
	public static void logOn() throws Exception {

		logOnNewUser(new String[] { AuthenticationPermission.LOGON
				.getResourceKey() });
	}

	@AfterClass
	public static void logOff() throws JsonParseException,
			JsonMappingException, IOException {
		logoff();
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionCurrentRealmGroupList()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/groups/list");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionCurrentRealmGroupsUser()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/groups/user/"
				+ getSession().getCurrentPrincipal().getId());
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionCurrentRealmUsersList()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/users/list");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionCurrentRealmUsersTable()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/users/table");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionCurrentRealmGroupsTable()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/groups/table");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionCurrentRealmUsersGroup()
			throws ClientProtocolException, IOException {

		JsonResourceList json = getMapper().readValue(
				doGet("/hypersocket/api/currentRealm/groups/list"),
				JsonResourceList.class);

		doGet("/hypersocket/api/currentRealm/users/group/"
				+ json.getResources()[0].getId());
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionCurrentRealmUserTemplate()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/user/template/local");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionCurrentRealmGroupTemplate()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/group/template/local");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionCurrentRealmUserProperties()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/user/properties/"
				+ getSession().getCurrentPrincipal().getId());
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionCurrentRealmUserProfile()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/user/profile");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionCurrentRealmUserProfilePost() throws Exception {

		PropertyItem item = new PropertyItem();
		item.setId("user.phone");
		item.setValue("666");
		PropertyItem[] items = { item };
		doPostJson("/hypersocket/api/currentRealm/user/profile", items);
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionCurrentRealmIdGroup()
			throws ClientProtocolException, IOException {
		JsonResourceList json = getMapper().readValue(
				doGet("/hypersocket/api/currentRealm/groups/list"),
				JsonResourceList.class);

		doGet("/hypersocket/api/currentRealm/group/"
				+ json.getResources()[0].getId());
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionCurrentRealmUserId()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/user/"
				+ getSession().getCurrentPrincipal().getId());
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionCurrentRealmGroupPost() throws Exception {

		GroupUpdate group = new GroupUpdate();
		group.setName("newGroup");
		Long[] users = { getSession().getCurrentPrincipal().getId() };
		group.setUsers(users);
		doPostJson("/hypersocket/api/currentRealm/group", group);
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionCurrentRealmGroupDelete()
			throws ClientProtocolException, IOException {

		JsonResourceList json = getMapper().readValue(
				doGet("/hypersocket/api/currentRealm/groups/list"),
				JsonResourceList.class);

		doDelete("/hypersocket/api/currentRealm/group/"
				+ json.getResources()[0].getId());
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionCurrentRealmDeleteUser()
			throws ClientProtocolException, IOException {
		doDelete("/hypersocket/api/currentRealm/user/"
				+ getSession().getCurrentPrincipal().getId());
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionCurrentRealmUserPost() throws Exception {

		UserUpdate user = new UserUpdate();
		user.setName("newUser");
		user.setPassword("newUserPass");
		user.setProperties(new PropertyItem[0]);
		user.setGroups(new Long[0]);

		doPostJson("/hypersocket/api/currentRealm/user", user);

	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionCurrentRealmCredentialsPost()
			throws ClientProtocolException, IOException, IllegalStateException,
			URISyntaxException {
		CredentialsUpdate credentialsUpdate = new CredentialsUpdate();

		credentialsUpdate.setForceChange(false);
		credentialsUpdate.setPassword("newPass");

		credentialsUpdate.setPrincipalId(getSession().getCurrentPrincipal().getId());

		doPostJson("/hypersocket/api/currentRealm/user/credentials",
				credentialsUpdate);

	}

}
