package com.hypersocket.tests.json.currentRealm;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.hypersocket.json.JsonResourceList;
import com.hypersocket.properties.json.PropertyItem;
import com.hypersocket.realm.json.CredentialsUpdate;
import com.hypersocket.realm.json.GroupUpdate;
import com.hypersocket.realm.json.UserUpdate;
import com.hypersocket.tests.AbstractServerTest;

public class UnauthorizedTests extends AbstractServerTest {

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedCurrentRealmGroupList()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/groups/list");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedCurrentRealmGroupsUser()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/groups/user/"
				+ getAuxSession().getCurrentPrincipal().getId());
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedCurrentRealmUsersList()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/users/list");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedCurrentRealmUsersTable()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/users/table");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedCurrentRealmGroupsTable()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/groups/table");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedCurrentRealmUsersGroup()
			throws ClientProtocolException, IOException {

		JsonResourceList json = getMapper().readValue(
				doGet("/hypersocket/api/currentRealm/groups/list"),
				JsonResourceList.class);

		doGet("/hypersocket/api/currentRealm/users/group/"
				+ json.getResources()[0].getId());
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedCurrentRealmUserTemplate()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/user/template/local");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedCurrentRealmGroupTemplate()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/group/template/local");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedCurrentRealmUserProperties()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/user/properties/"
				+ getAuxSession().getCurrentPrincipal().getId());
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedCurrentRealmUserProfile()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/user/profile");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedCurrentRealmUserProfilePost() throws Exception {

		PropertyItem item = new PropertyItem();
		item.setId("user.phone");
		item.setValue("666");
		PropertyItem[] items = { item };
		doPostJson("/hypersocket/api/currentRealm/user/profile", items);
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedCurrentRealmIdGroup()
			throws ClientProtocolException, IOException {
		JsonResourceList json = getMapper().readValue(
				doGet("/hypersocket/api/currentRealm/groups/list"),
				JsonResourceList.class);

		doGet("/hypersocket/api/currentRealm/group/"
				+ json.getResources()[0].getId());
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedCurrentRealmUserId()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/currentRealm/user/"
				+ getAuxSession().getCurrentPrincipal().getId());
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedCurrentRealmGroupPost() throws Exception {

		GroupUpdate group = new GroupUpdate();
		group.setName("newGroup");
		Long[] users = { getAuxSession().getCurrentPrincipal().getId() };
		group.setUsers(users);
		doPostJson("/hypersocket/api/currentRealm/group", group);
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedCurrentRealmGroupDelete()
			throws ClientProtocolException, IOException {

		JsonResourceList json = getMapper().readValue(
				doGet("/hypersocket/api/currentRealm/groups/list"),
				JsonResourceList.class);

		doDelete("/hypersocket/api/currentRealm/group/"
				+ json.getResources()[0].getId());
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedCurrentRealmDeleteUser()
			throws ClientProtocolException, IOException {
		doDelete("/hypersocket/api/currentRealm/user/"
				+ getAuxSession().getCurrentPrincipal().getId());
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedCurrentRealmUserPost() throws Exception {

		UserUpdate user = new UserUpdate();
		user.setName("newUser");
		user.setPassword("newUserPass");
		user.setProperties(new PropertyItem[0]);
		user.setGroups(new Long[0]);

		doPostJson("/hypersocket/api/currentRealm/user", user);

	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedCurrentRealmCredentialsPost()
			throws ClientProtocolException, IOException, IllegalStateException,
			URISyntaxException {
		CredentialsUpdate credentialsUpdate = new CredentialsUpdate();

		credentialsUpdate.setForceChange(false);
		credentialsUpdate.setPassword("newPass");

		credentialsUpdate
				.setPrincipalId(getAuxSession().getCurrentPrincipal().getId());

		doPostJson("/hypersocket/api/currentRealm/user/credentials",
				credentialsUpdate);

	}
}
