package com.hypersocket.tests.json.realms;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URISyntaxException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;
import org.springframework.util.Assert;

import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.properties.json.PropertyItem;
import com.hypersocket.realm.json.RealmUpdate;
import com.hypersocket.tests.AbstractServerTest;

public class AbstractPermissionsTest extends AbstractServerTest {

	@Test
	public void tryRealmId() throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/realms/realm/"
				+ getSession().getCurrentRealm().getId());
	}

	@Test
	public void tryRealmList() throws ClientProtocolException, IOException {

		doGet("/hypersocket/api/realms/list");
	}

	@Test
	public void tryRealmsTable() throws ClientProtocolException, IOException {

		doGet("/hypersocket/api/realms/table");
	}

	@Test
	public void tryRealmsTemplate() throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/realms/template/local");
	}

	@Test
	public void tryRealmPropertiesId() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/realms/realm/properties/"
				+ getSession().getCurrentRealm().getId());
	}

	@Test
	public void tryRealmPost() throws ClientProtocolException, IOException,
			IllegalStateException, URISyntaxException {
		RealmUpdate realm = new RealmUpdate();
		realm.setName("newRealm");
		realm.setProperties(new PropertyItem[0]);
		realm.setType("local");

		JsonResourceStatus json = getMapper().readValue(
				doPostJson("/hypersocket/api/realms/realm", realm),
				JsonResourceStatus.class);
		assertTrue(json.isSuccess());
		Assert.notNull(json.getResource().getId());
		assertEquals("newRealm", json.getResource().getName());

	}

	@Test
	public void tryRealmProviders() throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/realms/providers");
	}

	@Test
	public void tryRealmDeleteId() throws Exception {
		JsonResourceStatus json = createRealm("realmName");

		doDelete("/hypersocket/api/realms/realm/" + json.getResource().getId());
	}
}
