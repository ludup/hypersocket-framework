package com.hypersocket.tests.realms;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.Assert;

import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.properties.json.PropertyItem;
import com.hypersocket.realm.json.RealmUpdate;
import com.hypersocket.tests.AbstractServerTest;

public class WithAdminPermissionTests extends AbstractServerTest {

	@BeforeClass
	public static void LogOn() throws Exception {
		logon("Default", "admin", "Password123?");
	}

	@AfterClass
	public static void logOff() throws JsonParseException,
			JsonMappingException, IOException {
		logoff();
	}

	@Test
	public void tryWithAdminPermissionRealmId() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/realms/realm/"
				+ getSession().getCurrentRealm().getId());
	}

	@Test
	public void tryWithAdminPermissionRealmList()
			throws ClientProtocolException, IOException {

		doGet("/hypersocket/api/realms/list");
	}

	@Test
	public void tryWithAdminPermissionRealmsTable()
			throws ClientProtocolException, IOException {

		doGet("/hypersocket/api/realms/table");
	}

	@Test
	public void tryWithAdminPermissionRealmsTemplate()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/realms/template/local");
	}

	@Test
	public void tryWithAdminPermissionRealmPropertiesId()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/realms/realm/properties/"
				+ getSession().getCurrentRealm().getId());
	}

	@Test
	public void tryWithAdminPermissionRealmPost()
			throws ClientProtocolException, IOException, IllegalStateException,
			URISyntaxException {
		RealmUpdate realm = new RealmUpdate();
		realm.setName("newrealm");
		realm.setProperties(new PropertyItem[0]);
		realm.setType("local");

		JsonResourceStatus json = getMapper().readValue(
				doPostJson("/hypersocket/api/realms/realm", realm),
				JsonResourceStatus.class);
		Assert.notNull(json.getResource().getId());

	}

	@Test
	public void tryWithAdminPermissionRealmProviders()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/realms/providers");
	}

	@Test
	public void tryWithAdminPermissionRealmDeleteId()
			throws ClientProtocolException, IOException {
		doDelete("/hypersocket/api/realms/realm/"
				+ getSession().getCurrentRealm().getId());
	}
}
