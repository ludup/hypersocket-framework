package com.hypersocket.tests.realms;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hypersocket.json.JsonLogonResult;
import com.hypersocket.json.JsonSession;
import com.hypersocket.realm.Realm;
import com.hypersocket.tests.AbstractServerTest;

public class UnauthorizedTests extends AbstractServerTest {

	private static JsonSession auxSession;

	@BeforeClass
	public static void LogOn() throws Exception {
		String logonJson = doPost("/hypersocket/api/logon",
				new BasicNameValuePair("username", "admin"),
				new BasicNameValuePair("password", "Password123?"));

		JsonLogonResult logon = getMapper().readValue(logonJson,
				JsonLogonResult.class);
		auxSession = logon.getSession();
		logoff();
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedRealmId() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/realms/realm/"
				+ auxSession.getCurrentRealm().getId());
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedRealmList() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/realms/list");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedRealmsTable() throws ClientProtocolException,
			IOException {

		doGet("/hypersocket/api/realms/table");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedRealmsTemplate() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/realms/template/local");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedRealmPropertiesId()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/realms/realm/properties/"
				+ auxSession.getCurrentRealm().getId());
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
	public void tryUnauthorizedRealmProviders() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/realms/providers");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedRealmDeleteId() throws ClientProtocolException,
			IOException {
		doDelete("/hypersocket/api/realms/realm/"
				+ auxSession.getCurrentRealm().getId());
	}
}
