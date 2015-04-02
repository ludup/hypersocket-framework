package com.hypersocket.tests.json.realms;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.hypersocket.auth.AuthenticationPermission;
import com.hypersocket.properties.json.PropertyItem;
import com.hypersocket.realm.json.RealmUpdate;
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
	public void tryNoPermissionRealmId() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/realms/realm/"
				+ getSession().getCurrentRealm().getId());
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionRealmList() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/realms/list");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionRealmsTable() throws ClientProtocolException,
			IOException {

		doGet("/hypersocket/api/realms/table");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionRealmsTemplate() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/realms/template/local");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionRealmPropertiesId()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/realms/realm/properties/"
				+ getSession().getCurrentRealm().getId());
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionRealmPost() throws ClientProtocolException,
			IOException, IllegalStateException, URISyntaxException {
		RealmUpdate realm = new RealmUpdate();
		realm.setName("newRealm");
		realm.setProperties(new PropertyItem[0]);
		realm.setType("local");

		doPostJson("/hypersocket/realms/realm", realm);

	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionRealmProviders() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/realms/providers");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissionRealmDeleteId() throws ClientProtocolException,
			IOException {
		doDelete("/hypersocket/api/realms/realm/"
				+ getSession().getCurrentRealm().getId());
	}
}
