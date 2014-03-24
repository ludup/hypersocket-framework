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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import com.hypersocket.json.AuthenticationRequiredResult;
import com.hypersocket.json.AuthenticationResult;
import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.netty.Main;
import com.hypersocket.permissions.json.RoleUpdate;
import com.hypersocket.properties.json.PropertyItem;
import com.hypersocket.realm.json.CredentialsUpdate;
import com.hypersocket.realm.json.GroupUpdate;
import com.hypersocket.realm.json.UserUpdate;
import com.hypersocket.util.OverridePropertyPlaceholderConfigurer;

public class AbstractServerTest {

	static File tmp;
	static Main main;
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

		System.out
				.println("Integration test server is running. Changing admin password to Password123?");

		logon("Default", "admin", "admin", true, "Password123?");

		System.out.println("Logging out");

		logoff();

		System.out.println("Integration test server ready for tests");
	}

	public static HttpClient getHttpClient() {
		return HttpClients.custom().setDefaultCookieStore(cookieStore).build();
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

	protected static void logon(String realm, String username, String password)
			throws Exception {
		logon(realm, username, password, false, null);
	}

	protected static void logon(String realm, String username, String password,
			boolean expectChangePassword, String newPassword) throws Exception {

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

		if (expectChangePassword) {
			// We should now be logged on
			Assert.assertFalse(
					"The authentication should have failed because password change was expected",
					logonResult.getSuccess());

			logonJson = doPost("/hypersocket/api/logon",
					new BasicNameValuePair("password", newPassword),
					new BasicNameValuePair("confirmPassword", newPassword));

			debugJSON(logonJson);

			logonResult = mapper.readValue(logonJson,
					AuthenticationResult.class);

		}

		// We should now be logged on
		Assert.assertTrue("The user should be logged on but was not",
				logonResult.getSuccess());

	}

	protected static String debugJSON(String json) throws JsonParseException,
			JsonMappingException, IOException {
		Object obj = mapper.readValue(json, Object.class);
		String ret = mapper.defaultPrettyPrintingWriter()
				.writeValueAsString(obj);
		System.out.println(ret);
		return ret;
		
	}

	protected static void logoff() throws JsonParseException,
			JsonMappingException, IOException {

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

		CloseableHttpClient httpClient = (CloseableHttpClient) getHttpClient();
		CloseableHttpResponse response = (CloseableHttpResponse) httpClient
				.execute(login);
		
		System.out.println("Response: " + response.getStatusLine().toString());
		
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new ClientProtocolException(
					"Expected status code 200 for doPost");
		}

		try {
			return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		} finally {
			response.close();
			httpClient.close();
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

		CloseableHttpClient httpClient = (CloseableHttpClient) getHttpClient();
		CloseableHttpResponse response = (CloseableHttpResponse) httpClient
				.execute(httpget);

		System.out.println("Response: " + response.getStatusLine().toString());
		
		try {
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new ClientProtocolException(
						"Expected status code 200 for doGet");
			}

			return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		} finally {
			response.close();
			httpClient.close();
		}
	}

	protected static <T> T doGet(String url, Class<T> clz)
			throws ClientProtocolException, IOException {
		return mapper.readValue(doGet(url), clz);
	}

	protected static String doPost(String url, String json)
			throws URISyntaxException, ClientProtocolException, IOException {

		if (!url.startsWith("/")) {
			url = "/" + url;
		}

		HttpPost postMethod = new HttpPost("http://localhost:"
				+ main.getServer().getActualHttpPort() + url);

		StringEntity se = new StringEntity("JSON: " + json.toString());
		se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
				"application/json"));

		postMethod.setEntity(se);

		CloseableHttpClient httpClient = (CloseableHttpClient) getHttpClient();
		CloseableHttpResponse response = (CloseableHttpResponse) httpClient
				.execute(postMethod);

		System.out.println("Response: " + response.getStatusLine().toString());
		
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new ClientProtocolException(
					"Expected status code 200 for doPost");
		}

		try {
			return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		} finally {
			response.close();
			httpClient.close();
		}

	}

	protected static String doPostJson(String url, Object jsonObject)
				throws URISyntaxException, IllegalStateException, IOException {
	
			if (!url.startsWith("/")) {
				url = "/" + url;
			}
	
			String json = mapper.writeValueAsString(jsonObject);
	
			StringEntity requestEntity = new StringEntity(json,
					ContentType.APPLICATION_JSON);
	
			HttpUriRequest request = RequestBuilder
					.post()
					.setUri(new URI("http://localhost:"
							+ main.getServer().getActualHttpPort() + url))
					.setEntity(requestEntity).build();
	
			System.out.println("Executing request " + request.getRequestLine());
	
			CloseableHttpClient httpClient = (CloseableHttpClient) getHttpClient();
			CloseableHttpResponse response = (CloseableHttpResponse) httpClient
					.execute(request);
			
			System.out.println("Response: " + response.getStatusLine().toString());
			
			if (response.getStatusLine().getStatusCode() != 200) {
				
				throw new ClientProtocolException(
						"Expected status code 200 for doPost");
			}
			try {
				return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
			} finally {
				response.close();
				httpClient.close();
			}
		}

	protected static JsonResourceStatus createUser(String realm, String username)
			throws Exception {
		UserUpdate x = new UserUpdate();
		// x.setId(id); // do not set
		x.setName(username);
		x.setGroups(new Long[0]);

		PropertyItem propItem1 = new PropertyItem();
		propItem1.setId("user.fullname");
		propItem1.setValue("user");

		PropertyItem propItem2 = new PropertyItem();
		propItem2.setId("user.email");
		propItem2.setValue("");

		PropertyItem[] propArray = { propItem1, propItem2 };
		x.setProperties(propArray);

		String newUserJson = doPostJson("/hypersocket/api/user", x);
		debugJSON(newUserJson);

		JsonResourceStatus newUserJsonResourceStatus = mapper.readValue(
				newUserJson, JsonResourceStatus.class);
		return newUserJsonResourceStatus;

	}

	protected static void changePassword(String password,
			JsonResourceStatus json) throws Exception {

		CredentialsUpdate credentialsUpdate = new CredentialsUpdate();

		credentialsUpdate.setForceChange(false);
		credentialsUpdate.setPassword(password);

		credentialsUpdate.setPrincipalId(json.getResource().getId());

		String changePasswordJson = doPostJson("/hypersocket/api/credentials",
				credentialsUpdate);
		debugJSON(changePasswordJson);

	}

	protected static JsonResourceStatus createGroup(String groupname)
			throws Exception {
		GroupUpdate group = new GroupUpdate();
		group.setName(groupname);
		group.setUsers(new Long[0]);

		String newGroupJson = doPostJson("/hypersocket/api/group", group);

		JsonResourceStatus newGroupJsonResourceStatus = mapper.readValue(
				newGroupJson, JsonResourceStatus.class);
		debugJSON(newGroupJson);

		debugJSON(newGroupJson);
		return newGroupJsonResourceStatus;

	}

	protected static void addUserToGroup(JsonResourceStatus jsonGroup,
			JsonResourceStatus jsonUser) throws Exception {

		GroupUpdate groupUpdate = new GroupUpdate();
		groupUpdate.setName(jsonGroup.getResource().getName());
		groupUpdate.setId(jsonGroup.getResource().getId());
		Long[] groupUsers = { jsonUser.getResource().getId() };
		groupUpdate.setUsers(groupUsers);

		String addUserToGroupJson = doPostJson("/hypersocket/api/group",
				groupUpdate);
		debugJSON(addUserToGroupJson);

	}

	protected static void addUsersToGroup(JsonResourceStatus jsonGroup,
			Long[] users) throws Exception {

		GroupUpdate groupUpdate = new GroupUpdate();
		groupUpdate.setName(jsonGroup.getResource().getName());
		groupUpdate.setId(jsonGroup.getResource().getId());

		groupUpdate.setUsers(users);

		String addUserToGroupJson = doPostJson("/hypersocket/api/group",
				groupUpdate);
		debugJSON(addUserToGroupJson);

	}

	protected static JsonResourceStatus createRole(String rolename,
			Long[] permissions) throws Exception {
		RoleUpdate role = new RoleUpdate();
		role.setName(rolename);
		// role.setPermissions(new Long[0]);
		role.setPermissions(permissions);
		role.setUsers(new Long[0]);
		role.setGroups(new Long[0]);

		String newRoleJson = doPostJson("/hypersocket/api/role", role);
		JsonResourceStatus newRoleJsonResourceStatus = mapper.readValue(
				newRoleJson, JsonResourceStatus.class);
		debugJSON(newRoleJson);
		return newRoleJsonResourceStatus;

	}

	protected static void addUserToRole(JsonResourceStatus jsonRole,
			JsonResourceStatus jsonUser) throws Exception {

		RoleUpdate roleUpdate = new RoleUpdate();

		roleUpdate.setName(jsonRole.getResource().getName());
		roleUpdate.setId(jsonRole.getResource().getId());
		Long[] roleUsers = { jsonUser.getResource().getId() };
		roleUpdate.setUsers(roleUsers);
		roleUpdate.setGroups(new Long[0]);
		Long[] permissions = new Long[jsonRole.getResource().getPermissions().length];
		for (int x = 0; x < permissions.length; x++) {
			permissions[x] = jsonRole.getResource().getPermissions()[x].getId();
		}
		roleUpdate.setPermissions(permissions);

		String addUserToRoledJson = doPostJson("/hypersocket/api/role",
				roleUpdate);
		debugJSON(addUserToRoledJson);

	}

	protected static void addUsersToRole(JsonResourceStatus jsonRole,
			Long[] users) throws Exception {

		RoleUpdate roleUpdate = new RoleUpdate();

		roleUpdate.setName(jsonRole.getResource().getName());
		roleUpdate.setId(jsonRole.getResource().getId());
		roleUpdate.setUsers(users);
		roleUpdate.setGroups(new Long[0]);
		Long[] permissions = new Long[jsonRole.getResource().getPermissions().length];
		for (int x = 0; x < permissions.length; x++) {
			permissions[x] = jsonRole.getResource().getPermissions()[x].getId();
		}
		roleUpdate.setPermissions(permissions);

		String addUserToRoledJson = doPostJson("/hypersocket/api/role",
				roleUpdate);
		debugJSON(addUserToRoledJson);

	}

	protected static void addGroupToRole(JsonResourceStatus jsonRole,
			JsonResourceStatus jsonGroup) throws Exception {

		RoleUpdate roleUpdate = new RoleUpdate();

		roleUpdate.setName(jsonRole.getResource().getName());
		roleUpdate.setId(jsonRole.getResource().getId());
		Long[] roleGroups = { jsonGroup.getResource().getId() };
		roleUpdate.setGroups(roleGroups);
		roleUpdate.setUsers(new Long[0]);
		Long[] permissions = new Long[jsonRole.getResource().getPermissions().length];
		for (int x = 0; x < permissions.length; x++) {
			permissions[x] = jsonRole.getResource().getPermissions()[x].getId();
		}
		roleUpdate.setPermissions(permissions);

		String addGroupToRoledJson = doPostJson("/hypersocket/api/role",
				roleUpdate);
		debugJSON(addGroupToRoledJson);

	}
}
