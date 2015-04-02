package com.hypersocket.tests.json.i18n;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.hypersocket.tests.AbstractServerTest;

public class WithAdminPermissionTests extends AbstractServerTest {

	@BeforeClass
	public static void logOn() throws Exception {
		logon("System", "admin", "Password123?");
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
}
