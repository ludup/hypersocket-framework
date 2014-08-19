package com.hypersocket.tests.i18n;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hypersocket.i18n.Message;
import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.json.JsonRoleResourceStatus;
import com.hypersocket.tests.AbstractServerTest;

public class NoPermissionTests extends AbstractServerTest {

	@BeforeClass
	public static void logOn() throws Exception {

		logon("Default", "admin", "Password123?");
		JsonResourceStatus jsonCreateUser = createUser("Default", "user",
				"user", false);
		changePassword("user", jsonCreateUser);
		Long[] permissions = { getPermissionId("permission.logon") };
		JsonRoleResourceStatus jsonCreateRole = createRole("newrole",
				permissions);
		addUserToRole(jsonCreateRole.getResource(), jsonCreateUser);
		logoff();
		logon("Default", "user", "user");
	}

	@AfterClass
	public static void logOff() throws JsonParseException,
			JsonMappingException, IOException {
		logoff();
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissioni18n() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/i18n");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissioni18nLocale() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/i18n/en");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissioni18nLocales() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/i18n/locales");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissioni18nPostMessages() throws Exception {
		Message message = new Message();

		message.setBundle("Pin");
		message.setId("user.pin");
		message.setOriginal("Pin");
		message.setTranslated("Pin");

		Message[] messages = { message };
		doPostJson("/hypersocket/api/i18n/messages", messages);
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissioni18nSearchPattern() throws Exception {

		doGet("/hypersocket/api/i18n/search/proxy");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissioni18nSearch() throws Exception {

		doGet("/hypersocket/api/i18n/search");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryNoPermissioni18nStats() throws Exception {

		doGet("/hypersocket/api/i18n/stats");
	}
}
