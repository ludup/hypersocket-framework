package com.hypersocket.tests;

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

import com.hypersocket.json.JsonResource;
import com.hypersocket.json.JsonResourceList;
import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.properties.json.PropertyItem;
import com.hypersocket.realm.json.CredentialsUpdate;
import com.hypersocket.realm.json.GroupUpdate;
import com.hypersocket.realm.json.RealmUpdate;
import com.hypersocket.realm.json.UserUpdate;

public class WithAdminPermissionTests extends AbstractServerTest {

	@BeforeClass
	public static void createUser() throws Exception {
		logon("Default", "admin", "Password123?");

		// Creating users
		JsonResourceStatus user1 = createUser("Default", "user1");
		JsonResourceStatus user2 = createUser("Default", "user2");
		JsonResourceStatus user3 = createUser("Default", "user3");

		// Creating groups
		JsonResourceStatus group1 = createGroup("group1");
		JsonResourceStatus group2 = createGroup("group2");

		addUsersToGroup(group1, new Long[] { user1.getResource().getId(),
				user2.getResource().getId(), new Long(7) });

		addUsersToGroup(group2, new Long[] { user3.getResource().getId(),
				user2.getResource().getId(), new Long(7) });

		// Creating roles
		createRole("role1", new Long[] { new Long(21) });
		createRole("role2", new Long[] { new Long(21) });
	}

	@AfterClass
	public static void logOff() throws JsonParseException,
			JsonMappingException, IOException {
		logoff();
	}

	@Test
	public void tryWithAdminPermissionConfiguration()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/configuration");
	}

	@Test
	public void tryWithAdminPermissionNetworkInterfaces()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/networkInterfaces");

	}

	@Test
	public void tryWithAdminPermissionSslProtocols()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/sslProtocols");
	}

	@Test
	public void tryWithAdminPermissionSslCiphers()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/sslCiphers");
	}

	@Test
	public void tryWithAdminPermissionUserPost()
			throws ClientProtocolException, IOException, IllegalStateException,
			URISyntaxException {
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

		JsonResourceStatus json = mapper.readValue(
				doPostJson("/hypersocket/api/user", user),
				JsonResourceStatus.class);

		assertTrue("There was an error while creating a new user.",
				json.isSuccess());
	}

	@Test
	public void tryWithAdminPermissionCredentialsPost()
			throws ClientProtocolException, IOException, IllegalStateException,
			URISyntaxException {
		CredentialsUpdate credentialsUpdate = new CredentialsUpdate();

		credentialsUpdate.setForceChange(false);
		credentialsUpdate.setPassword("admin");

		credentialsUpdate.setPrincipalId(new Long(7)); // admin

		JsonResourceStatus json = mapper.readValue(
				doPostJson("/hypersocket/api/credentials", credentialsUpdate),
				JsonResourceStatus.class);

		assertTrue("There was an error while changing password.",
				json.isSuccess());

	}

	@Test
	public void tryWithAdminPermissionUserId() throws ClientProtocolException,
			IOException {
		UserUpdate user = mapper.readValue(doGet("/hypersocket/api/user/7"),
				UserUpdate.class); // admin
		assertEquals("admin", user.getName());
	}

	@Test
	public void tryWithAdminPermissionUserList()
			throws ClientProtocolException, IOException {
		JsonResourceList json = mapper.readValue(
				doGet("/hypersocket/api/users"), JsonResourceList.class);
		assertEquals(4, json.getResources().length); // admin, user1, user2,
														// user3
	}

	@Test
	public void tryWithAdminPermissionUsersGroupId()
			throws ClientProtocolException, IOException {
		JsonResourceList json = mapper.readValue(
				doGet("/hypersocket/api/users/13"), JsonResourceList.class); // group1
		assertEquals(3, json.getResources().length);
	}

	@Test
	public void tryWithAdminPermissionTableUsers()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/table/users?iDisplayStart=0&iDisplayLength=10&sEcho=1&iSortingCols=0&sSearch=");
	}

	@Test
	public void tryWithAdminPermissionUserTemplate()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/user/template/local");
	}

	@Test
	public void tryWithAdminPermissionUserProperties()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/user/properties/7"); // admin
	}

	@Test
	public void tryWithAdminPermissionGroupList()
			throws ClientProtocolException, IOException {
		JsonResourceList json = mapper.readValue(
				doGet("/hypersocket/api/groups"), JsonResourceList.class);
		assertEquals(3, json.getResources().length); // Administrators, group1,
														// group2
	}

	@Test
	public void tryWithAdminPermissionGroupsUserId()
			throws ClientProtocolException, IOException {
		JsonResourceList json = mapper.readValue(
				doGet("/hypersocket/api/groups/7"), JsonResourceList.class);
		assertEquals(3, json.getResources().length); // admin, user1, user2
	}

	@Test
	public void tryWithAdminPermissionGroupId() throws ClientProtocolException,
			IOException {
		JsonResource json = mapper.readValue(
				doGet("/hypersocket/api/group/13"), JsonResource.class); // group1
		assertEquals("group1", json.getName());
	}

	@Test
	public void tryWithAdminPermissionGroupPost()
			throws ClientProtocolException, IOException, IllegalStateException,
			URISyntaxException {
		GroupUpdate groupUpdate = new GroupUpdate();
		groupUpdate.setName("newgroup");
		groupUpdate.setUsers(new Long[0]);

		JsonResourceStatus json = mapper.readValue(
				doPostJson("/hypersocket/api/group", groupUpdate),
				JsonResourceStatus.class);
		assertTrue("There was an error while creating a new group.",
				json.isSuccess());
	}

	@Test
	public void tryWithAdminPermissionTableGroups()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/table/groups?iDisplayStart=0&iDisplayLength=10&sEcho=1&iSortingCols=0&sSearch=");
	}

	@Test
	public void tryWithAdminPermissionRealmPost()
			throws ClientProtocolException, IOException, IllegalStateException,
			URISyntaxException {
		RealmUpdate realm = new RealmUpdate();
		realm.setName("newrealm");
		realm.setType("local");
		realm.setProperties(new PropertyItem[0]);

		JsonResourceStatus json = mapper.readValue(
				debugJSON(doPostJson("/hypersocket/api/realm", realm)),
				JsonResourceStatus.class);
		
		assertTrue("There was an error while creating a new realm.",
				json.isSuccess());

	}

	@Test
	public void tryWithAdminPermissionRealmId() throws ClientProtocolException,
			IOException {
		JsonResource json = mapper.readValue(
				doGet("/hypersocket/api/realm/5"), JsonResource.class); // Default
		assertEquals("Default", json.getName());
	}

	@Test
	public void tryWithAdminPermissionRealmList()
			throws ClientProtocolException, IOException {
		JsonResourceList json = mapper.readValue(
				doGet("/hypersocket/api/realms"), JsonResourceList.class);
		assertEquals(1, json.getResources().length); // Default
	}

	@Test
	public void tryWithAdminPermissionRealmTemplate()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/realm/template/local");
	}

	@Test
	public void tryWithAdminPermissionRealmPropertiesId()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/realm/properties/5"); // Default
	}

	@Test
	public void tryWithAdminPermissionRealmProviders()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/realm/providers");
	}

	@Test
	public void tryWithAdminPermissionRoleList()
			throws ClientProtocolException, IOException {
		JsonResourceList json = mapper.readValue(
				doGet("/hypersocket/api/roles"), JsonResourceList.class);
		assertEquals(3, json.getResources().length); // Administrators, role1,
														// role2
	}
}
