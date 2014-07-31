package com.hypersocket.tests;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.hypersocket.properties.json.PropertyItem;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.json.CredentialsUpdate;
import com.hypersocket.realm.json.GroupUpdate;
import com.hypersocket.realm.json.UserUpdate;

public class UnauthorizedTests extends AbstractServerTest {

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedUserList() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/currentRealm/users/list");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedGroupList() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/currentRealm/groups/list");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedRoleList() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/roles/list");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedRealmList() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/realms/list");
	}

	// @Ignore
	// @Test(expected = AssertionError.class)
	// public void tryUnauthorizedLogon() throws Exception {
	// logon("Default", "admin", "Password123?");
	// JsonResourceStatus json = createUser("Default", "user");
	// changePassword("user", json);
	// logoff();
	// logon("Default", "user", "user");
	// }
	//
	// @Ignore
	// @Test(expected = ClientProtocolException.class)
	// public void tryUnauthorizedLogonUserConfiguration() throws Exception {
	// logon("Default", "admin", "Password123?");
	//
	// logoff();
	// logon("Default", "userConfiguration", "userConfiguration");
	// doGet("/hypersocket/api/configuration");
	// logoff();
	// }
	//
	// @Ignore
	// @Test(expected = ClientProtocolException.class)
	// public void tryUnauthorizedLogonGroupConfiguration() throws Exception {
	// logon("Default", "admin", "Password123?");
	// JsonResourceStatus jsonCreateUser = createUser("Default",
	// "userGroupConfiguration");
	// changePassword("userGroupConfiguration", jsonCreateUser);
	// JsonResourceStatus jsonCreateGroup = createGroup("newgroup");
	// addUserToGroup(jsonCreateGroup, jsonCreateUser);
	// Long[] permissions = { new Long(21) };
	// JsonResourceStatus jsonCreateRole = createRole("newrole", permissions);
	// addGroupToRole(jsonCreateRole, jsonCreateGroup);
	// logoff();
	// logon("Default", "userGroupConfiguration", "userGroupConfiguration");
	// doGet("/hypersocket/api/configuration");
	// logoff();
	// }

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedConfiguration() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/configuration");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedRestart() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/server/restart/60");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedShutdown() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/server/shutdown/60");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedNetworkInterfaces()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/server/networkInterfaces");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedSslProtocols() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/server/sslProtocols");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedSslCiphers() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/server/sslCiphers");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedUserListPost() throws ClientProtocolException,
			IOException, IllegalStateException, URISyntaxException {
		UserUpdate user = new UserUpdate();
		user.setName("user");
		user.setGroups(new Long[0]);

		PropertyItem propItem1 = new PropertyItem();
		propItem1.setId("user.fullname");
		propItem1.setValue("user");

		PropertyItem propItem2 = new PropertyItem();
		propItem2.setId("user.email");
		propItem2.setValue("");

		PropertyItem[] propArray = { propItem1, propItem2 };
		user.setProperties(propArray);

		doPostJson("/hypersocket/api/currentRealm/user", user);

	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedCredentialsPost()
			throws ClientProtocolException, IOException, IllegalStateException,
			URISyntaxException {
		CredentialsUpdate credentialsUpdate = new CredentialsUpdate();

		credentialsUpdate.setForceChange(false);
		credentialsUpdate.setPassword("admin");

		credentialsUpdate.setPrincipalId(new Long(6));

		doPostJson("/hypersocket/api/currentRealm/user/credentials", credentialsUpdate);

	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedUserId() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/currentRealm/user/7");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedUsersGroupId() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/currentRealm/users/group/6");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedTableUsers() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/currentRealm/users/table");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedUserProperties() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/currentRealm/user/properties/7");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedGroupsUserId() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/currentRealm/groups/user/7");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedGroupId() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/currentRealm/group/6");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedGroupPost() throws ClientProtocolException,
			IOException, IllegalStateException, URISyntaxException {
		GroupUpdate groupUpdate = new GroupUpdate();
		groupUpdate.setName("newgroup");
		groupUpdate.setUsers(new Long[0]);

		doPostJson("/hypersocket/api/currentRealm/group", groupUpdate);

	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedTableGroups() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/currentRealm/groups/table");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedRealmPost() throws ClientProtocolException,
			IOException, IllegalStateException, URISyntaxException {
		Realm realm = new Realm();
		realm.setName("newrealm");
		realm.setDeleted(false);
		realm.setHidden(false);
		realm.setResourceCategory("");

		doPostJson("/hypersocket/realms/realm", realm);

	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedRealmId() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/realms/realm/5");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedRealmPropertiesId()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/realms/realm/properties/5");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedRealmProviders() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/realms/providers");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedUserTemplate() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/currentRealm/user/template/local");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedRealmTemplate() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/realms/template/local");
	}
	
}
