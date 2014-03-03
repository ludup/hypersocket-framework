package com.hypersocket.tests;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import com.hypersocket.json.AuthenticationRequiredResult;
import com.hypersocket.json.AuthenticationResult;
import com.hypersocket.netty.Main;
import com.hypersocket.util.OverridePropertyPlaceholderConfigurer;

public class AbstractServerTest {

	static File tmp;
	static Main main;
	static HttpClient httpClient;
	static BasicCookieStore cookieStore;
	static ObjectMapper mapper = new ObjectMapper();

	@BeforeClass
	public static void startup() throws Exception {

		tmp = Files.createTempDirectory("hypersocket").toFile();
		tmp.mkdirs();

		File conf = new File(tmp, "conf");

		File data = new File(tmp, "data");

		FileUtils.copyDirectory(new File("default-conf"), conf);

		StringBuffer buf = new StringBuffer();
		buf.append("http.port=0\r\n"); // Generate random port
		buf.append("https.port=0\r\n"); // Generate random port
		buf.append("require.https=false\r\n"); // Non SSL for now
		
		FileUtils.writeStringToFile(new File(conf, "hypersocket.properties"),
				buf.toString());

		buf.setLength(0);

		buf.append("jdbc.driver.className=org.apache.derby.jdbc.EmbeddedDriver\r\n");
		buf.append("jdbc.url=jdbc:derby:" + data.getAbsolutePath()
				+ ";create=true\r\n");
		buf.append("jdbc.username=hypersocket\r\n");
		buf.append("jdbc.password=hypersocket\r\n");
		buf.append("jdbc.hibernate.dialect=com.hypersocket.derby.DefaultClobDerbyDialect\r\n");

		OverridePropertyPlaceholderConfigurer.setOverrideFile(new File(conf,
				"database.properties"));

		FileUtils.writeStringToFile(new File(conf, "database.properties"),
				buf.toString());

		main = new Main(new Runnable() {

			@Override
			public void run() {
				System.out.println("Starting intergration test server");
			}

		}, new Runnable() {

			@Override
			public void run() {
				System.out.println("Stopping intergration test server");
			}

		});

		main.setConfigurationDir(conf);
		main.run();

		cookieStore = new BasicCookieStore();
		httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore)
				.build();

		System.out.println("Integration test server is running. Changing admin password to Password123?");
		
		logon("Default", "admin", "admin", true, "Password123?");
		
		System.out.println("Logging out");
		
		logoff();
		
		System.out.println("Integration test server ready for tests");
	}

	@AfterClass
	public static void shutdown() throws IOException {

		if (main != null) {
			main.shutdownServer();
		}
		if (tmp != null && tmp.exists()) {
			FileUtils.deleteDirectory(tmp);
		}
	}

	protected static void logon(String realm, String username, String password) throws Exception {
		logon(realm, username, password, false, null);
	}
	
	protected static void logon(String realm, String username, String password, boolean expectChangePassword, String newPassword)
			throws Exception {

		String json = doGet("/hypersocket/api/logon");
		debugJSON(json);

		AuthenticationResult result = mapper.readValue(json,
				AuthenticationResult.class);

		// We should not already be logged in
		Assert.assertFalse(result.getSuccess());

		AuthenticationRequiredResult resultWithForm = mapper.readValue(json,
				AuthenticationRequiredResult.class);

		// The form should be a username and password form
		Assert.assertEquals("usernameAndPassword", resultWithForm
				.getFormTemplate().getResourceKey());

		// Check form has 3 elements, realm, username and password
		Assert.assertEquals(3, resultWithForm.getFormTemplate()
				.getInputFields().size());

		String logonJson = doPost("/hypersocket/api/logon",
				new BasicNameValuePair("realm", realm), new BasicNameValuePair(
						"username", username), new BasicNameValuePair(
						"password", password));

		debugJSON(logonJson);
		
		AuthenticationResult logonResult = mapper.readValue(logonJson,
				AuthenticationResult.class);

		if(expectChangePassword) {
			// We should now be logged on
			Assert.assertFalse("The authentication should have failed because password change was expected", logonResult.getSuccess());
			
			logonJson = doPost("/hypersocket/api/logon",
						new BasicNameValuePair(
								"password", newPassword), new BasicNameValuePair(
								"confirmPassword", newPassword));

			debugJSON(logonJson);
			
			logonResult = mapper.readValue(logonJson,
					AuthenticationResult.class);

		} 
		
		// We should now be logged on
		Assert.assertTrue("The user should be logged on but was not",logonResult.getSuccess());	
		
	}

	protected static void debugJSON(String json) throws JsonParseException,
			JsonMappingException, IOException {
		Object obj = mapper.readValue(json, Object.class);
		System.out.println(mapper.defaultPrettyPrintingWriter()
				.writeValueAsString(obj));
	}

	protected static void logoff() throws JsonParseException, JsonMappingException, IOException {
		
		// Will throw an exception if user is not logged on
		doGet("/hypersocket/api/logoff");
	}

	protected static String doPost(String url, NameValuePair... postVariables)
			throws URISyntaxException, ClientProtocolException, IOException {

		if (!url.startsWith("/")) {
			url = "/" + url;
		}

		HttpUriRequest login = RequestBuilder
				.post()
				.setUri(new URI("http://localhost:"
						+ main.getServer().getActualHttpPort() + url))
				.addParameters(postVariables).build();
		
		System.out.println("Executing request " + login.getRequestLine());
		
		CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(login);

		if(response.getStatusLine().getStatusCode()!=200) {
			throw new ClientProtocolException("Expected status code 200 for doPost");
		}
		
		try {
			return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		} finally {
			response.close();
		}

	}

	protected static String doGet(String url) throws ClientProtocolException,
			IOException {

		if (!url.startsWith("/")) {
			url = "/" + url;
		}

		HttpGet httpget = new HttpGet("http://localhost:"
				+ main.getServer().getActualHttpPort() + url);

		System.out.println("Executing request " + httpget.getRequestLine());

		CloseableHttpResponse response = (CloseableHttpResponse) httpClient.execute(httpget);
		try {
			if(response.getStatusLine().getStatusCode()!=200) {
				throw new ClientProtocolException("Expected status code 200 for doGet");
			}

			return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		} finally {
			response.close();
		}
	}

	protected static <T> T doGet(String url, Class<T> clz)
			throws ClientProtocolException, IOException {
		return mapper.readValue(doGet(url), clz);
	}

}
