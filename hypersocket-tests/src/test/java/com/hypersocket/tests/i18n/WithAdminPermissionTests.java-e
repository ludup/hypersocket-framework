package com.hypersocket.tests.i18n;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.util.Assert;

import com.hypersocket.i18n.Message;
import com.hypersocket.json.JsonResourceStatus;
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
	public void tryWithAdminPermissioni18n() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/i18n");
	}

	@Test
	public void tryWithAdminPermissioni18nLocale()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/i18n/en");
	}

	@Test
	public void tryWithAdminPermissioni18nLocales()
			throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/i18n/locales");
	}

	@Test
	public void tryWithAdminPermissioni18nPostMessages() throws Exception {
		Message message = new Message();

		message.setBundle("Pin");
		message.setId("user.pin");
		message.setOriginal("Pin");
		message.setTranslated("Pin");

		Message[] messages = { message };

		JsonResourceStatus json = getMapper().readValue(
				doPostJson("/hypersocket/api/i18n/messages", messages),
				JsonResourceStatus.class);
		Assert.isTrue(json.isSuccess());
	}

	@Test
	public void tryWithAdminPermissioni18nSearchPattern() throws Exception {

		doGet("/hypersocket/api/i18n/search/proxy");
	}

	@Test
	public void tryWithAdminPermissioni18nSearch() throws Exception {

		doGet("/hypersocket/api/i18n/search");
	}

	@Test
	public void tryWithAdminPermissioni18nStats() throws Exception {

		doGet("/hypersocket/api/i18n/stats");
	}

}
