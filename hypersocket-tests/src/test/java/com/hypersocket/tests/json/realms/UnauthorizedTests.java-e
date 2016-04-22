package com.hypersocket.tests.json.realms;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.hypersocket.properties.json.PropertyItem;
import com.hypersocket.realm.json.RealmUpdate;
import com.hypersocket.tests.AbstractServerTest;

public class UnauthorizedTests extends AbstractServerTest {

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedRealmId() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/realms/realm/"
				+ getAuxSession().getCurrentRealm().getId());
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
				+ getAuxSession().getCurrentRealm().getId());
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedRealmPost() throws ClientProtocolException,
			IOException, IllegalStateException, URISyntaxException {
		RealmUpdate realm = new RealmUpdate();
		realm.setName("newRealm");
		realm.setProperties(new PropertyItem[0]);
		realm.setType("local");

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
				+ getAuxSession().getCurrentRealm().getId());
	}
}
