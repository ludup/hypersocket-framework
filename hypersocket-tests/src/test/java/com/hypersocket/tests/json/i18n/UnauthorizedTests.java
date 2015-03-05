package com.hypersocket.tests.json.i18n;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.hypersocket.i18n.Message;
import com.hypersocket.tests.AbstractServerTest;

public class UnauthorizedTests extends AbstractServerTest {

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedi18n() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/i18n");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedi18nLocale() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/i18n/en");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedi18nLocales() throws ClientProtocolException,
			IOException {
		doGet("/hypersocket/api/i18n/locales");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedi18nPostMessages() throws Exception {
		Message message = new Message();

		message.setBundle("Pin");
		message.setId("user.pin");
		message.setOriginal("Pin");
		message.setTranslated("Pin");

		Message[] messages = { message };
		doPostJson("/hypersocket/api/i18n/messages", messages);
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedi18nSearchPattern() throws Exception {

		doGet("/hypersocket/api/i18n/search/proxy");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedi18nSearch() throws Exception {

		doGet("/hypersocket/api/i18n/search");
	}

	@Test(expected = ClientProtocolException.class)
	public void tryUnauthorizedi18nStats() throws Exception {

		doGet("/hypersocket/api/i18n/stats");
	}
}
