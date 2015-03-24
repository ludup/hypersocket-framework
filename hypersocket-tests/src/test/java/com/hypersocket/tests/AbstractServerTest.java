package com.hypersocket.tests;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.json.AuthenticationRequiredResult;
import com.hypersocket.json.AuthenticationResult;
import com.hypersocket.json.JsonLogonResult;
import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.json.JsonRole;
import com.hypersocket.json.JsonRoleResourceStatus;
import com.hypersocket.json.JsonSession;
import com.hypersocket.netty.Main;
import com.hypersocket.permissions.json.RoleUpdate;
import com.hypersocket.properties.json.PropertyItem;
import com.hypersocket.realm.json.CredentialsUpdate;
import com.hypersocket.realm.json.GroupUpdate;
import com.hypersocket.realm.json.RealmUpdate;
import com.hypersocket.realm.json.UserUpdate;
import com.hypersocket.tests.json.utils.MultipartObject;
import com.hypersocket.tests.json.utils.PropertyObject;
import com.hypersocket.util.OverridePropertyPlaceholderConfigurer;

public class AbstractServerTest {

	protected static File tmp;
	static Main main;
	static BasicCookieStore cookieStore;
	protected static ObjectMapper mapper = new ObjectMapper();
	protected static Long adminId;
	protected static File certificatesFolder;

	protected static JsonSession session;
	private static JsonSession auxSession;

	protected static JsonRole systemAdminRole;

	@BeforeClass
	public static void startup() throws Exception {

		tmp = Files.createTempDirectory("hypersocket").toFile();
		tmp.mkdirs();
		File conf = new File(tmp, "conf");

		File data = new File(tmp, "data");

		FileUtils.copyDirectory(new File("default-conf"), conf);
		certificatesFolder = new File(tmp, "cert_folder");
		certificatesFolder.mkdirs();
		FileUtils.copyDirectory(new File("certificates"), certificatesFolder);

		StringBuffer buf = new StringBuffer();
		buf.append("http.port=0\r\n"); // Generate random port
		buf.append("https.port=0\r\n"); // Generate random port
		buf.append("require.https=false\r\n"); // Non SSL for now

		FileUtils.writeStringToFile(new File(conf, "hypersocket.properties"),
				buf.toString());

		buf.setLength(0);

		buf.append("jdbc.driver.className=org.apache.derby.jdbc.EmbeddedDriver\r\n");
		String dbPath = data.getAbsolutePath();
		String strep = "\\\\\\\\";
		if (dbPath.indexOf("\\") > 0) {
			dbPath = dbPath.replaceAll("\\\\", strep);
		}
		buf.append("jdbc.url=jdbc:derby:" + dbPath + ";create=true\r\n");
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

		logon("System", "admin", "admin", true, "Password123?");
		adminId = getSession().getCurrentPrincipal().getId();
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
		try {// wait to close db connection before delete folder
			new Thread().wait(2000);
		} catch (Exception e) {
		}
		if (tmp != null && tmp.exists()) {
			try {
				FileUtils.deleteDirectory(tmp);
			} catch (Exception e) {
			}
		}

	}

	protected static void logon(String realm, String username, String password)
			throws Exception {
		logon(realm, username, password, false, null);
	}

	protected static void logon(String realm, String username, String password,
			boolean expectChangePassword, String newPassword) throws Exception {

		// main.getServer().
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

		// Check form has 2 elements, username and password
		Assert.assertEquals(2, resultWithForm.getFormTemplate()
				.getInputFields().size());

		String logonJson = doPost("/hypersocket/api/logon",
				new BasicNameValuePair("username", username),
				new BasicNameValuePair("password", password));

		debugJSON(logonJson);

		AuthenticationResult logonResult = mapper.readValue(logonJson,
				AuthenticationResult.class);
		if (logonResult.getSuccess()) {
			JsonLogonResult logon = mapper.readValue(logonJson,
					JsonLogonResult.class);
			session = logon.getSession();
			auxSession = logon.getSession();
		} else {
			session = null;
		}
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
			if (logonResult.getSuccess()) {
				JsonLogonResult logon = mapper.readValue(logonJson,
						JsonLogonResult.class);
				session = logon.getSession();
				auxSession = logon.getSession();
			} else {
				session = null;
			}

			if (getSystemAdminRole() == null) {
				systemAdminRole = mapper
						.readValue(
								doGet("/hypersocket/api/roles/byName/System%20Administrator"),
								JsonRole.class);
			}

		}

		// We should now be logged on
		Assert.assertTrue("The user should be logged on but was not",
				logonResult.getSuccess());

	}

	protected static String debugJSON(String json) throws JsonParseException,
			JsonMappingException, IOException {
		Object obj = mapper.readValue(json, Object.class);
		String ret = mapper.writerWithDefaultPrettyPrinter()
				.writeValueAsString(obj);
		System.out.println(ret);
		return ret;

	}

	protected static void logoff() throws JsonParseException,
			JsonMappingException, IOException {

		// Will throw an exception if user is not logged on
		doGet("/hypersocket/api/logoff");
		session = null;
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
						"Expected status code 200 for doGet ["
								+ response.getStatusLine().getStatusCode()
								+ "]");
			}

			return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		} finally {
			response.close();
			httpClient.close();
		}
	}

	protected static String doDelete(String url)
			throws ClientProtocolException, IOException {

		if (!url.startsWith("/")) {
			url = "/" + url;
		}

		HttpDelete httpdelete = new HttpDelete("http://localhost:"
				+ main.getServer().getActualHttpPort() + url);

		System.out.println("Executing request " + httpdelete.getRequestLine());

		CloseableHttpClient httpClient = (CloseableHttpClient) getHttpClient();
		CloseableHttpResponse response = (CloseableHttpResponse) httpClient
				.execute(httpdelete);

		System.out.println("Response: " + response.getStatusLine().toString());

		try {
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new ClientProtocolException(
						"Expected status code 200 for doGet ["
								+ response.getStatusLine().getStatusCode()
								+ "]");
			}

			return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		} finally {
			response.close();
			httpClient.close();
		}
	}

	protected static String doPostMultiparts(String url,
			PropertyObject[] properties, MultipartObject... files)
			throws ClientProtocolException, IOException {
		if (!url.startsWith("/")) {
			url = "/" + url;
		}
		HttpPost postMethod = new HttpPost("http://localhost:"
				+ main.getServer().getActualHttpPort() + url);
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		if (properties != null && properties.length > 0) {
			for (PropertyObject property : properties) {
				/**
				 * I've changed this because of deprecation warning. This may
				 * break the test.
				 */
				builder.addPart(property.getProertyName(), new StringBody(
						property.getPropertyValue(),
						ContentType.APPLICATION_FORM_URLENCODED));
			}
		}
		for (MultipartObject file : files) {
			builder.addPart(file.getProperty(), new FileBody(file.getFile()));
		}
		HttpEntity reqEntity = builder.build();
		postMethod.setEntity(reqEntity);
		System.out.println("Executing request " + postMethod.getRequestLine());
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

	protected static JsonResourceStatus createUser(String realm,
			String username, String password, boolean forceChange)
			throws Exception {
		return createUser(realm, username, password, forceChange, new Long[0]);
	}

	protected static JsonResourceStatus createUser(String realm,
			String username, String password, boolean forceChange, Long[] groups)
			throws Exception {
		UserUpdate x = new UserUpdate();
		x.setName(username);
		x.setGroups(groups);
		x.setForceChange(forceChange);
		x.setPassword(password);
		PropertyItem propItem1 = new PropertyItem();
		propItem1.setId("user.fullname");
		propItem1.setValue(username);

		PropertyItem propItem2 = new PropertyItem();
		propItem2.setId("user.email");
		propItem2.setValue("");

		PropertyItem[] propArray = { propItem1, propItem2 };
		x.setProperties(propArray);

		String newUserJson = doPostJson("/hypersocket/api/currentRealm/user", x);
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

		String changePasswordJson = doPostJson(
				"/hypersocket/api/currentRealm/user/credentials",
				credentialsUpdate);
		debugJSON(changePasswordJson);

	}

	protected static JsonResourceStatus createGroup(String groupname)
			throws Exception {
		GroupUpdate group = new GroupUpdate();
		group.setName(groupname);
		group.setUsers(new Long[0]);

		String newGroupJson = doPostJson("/hypersocket/api/currentRealm/group",
				group);

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

		String addUserToGroupJson = doPostJson(
				"/hypersocket/api/currentRealm/group", groupUpdate);
		debugJSON(addUserToGroupJson);

	}

	protected static void addUsersToGroup(JsonResourceStatus jsonGroup,
			Long[] users) throws Exception {

		GroupUpdate groupUpdate = new GroupUpdate();
		groupUpdate.setName(jsonGroup.getResource().getName());
		groupUpdate.setId(jsonGroup.getResource().getId());

		groupUpdate.setUsers(users);

		String addUserToGroupJson = doPostJson(
				"/hypersocket/api/currentRealm/group", groupUpdate);
		debugJSON(addUserToGroupJson);

	}

	protected static long getPermissionId(String resourceKey) throws Exception {

		String permissionJson = doGet("/hypersocket/api/permissions/permission/"
				+ resourceKey + "/");

		JsonResourceStatus status = mapper.readValue(permissionJson,
				JsonResourceStatus.class);

		if (!status.isSuccess()) {
			throw new Exception("Cannot retrieve permission id for "
					+ resourceKey);
		}

		return status.getResource().getId();
	}

	protected static JsonRoleResourceStatus createRole(String rolename,
			Long[] permissions) throws Exception {
		RoleUpdate role = new RoleUpdate();
		role.setName(rolename);
		// role.setPermissions(new Long[0]);
		role.setPermissions(permissions);
		role.setUsers(new Long[0]);
		role.setGroups(new Long[0]);

		String newRoleJson = doPostJson("/hypersocket/api/roles/role", role);
		JsonRoleResourceStatus newRoleJsonResourceStatus = mapper.readValue(
				newRoleJson, JsonRoleResourceStatus.class);
		debugJSON(newRoleJson);
		return newRoleJsonResourceStatus;
	}

	protected static JsonRole getRole(String roleName) throws Exception {
		String newRoleJson = doGet("/hypersocket/api/roles/byName/" + roleName);
		debugJSON(newRoleJson);
		return mapper.readValue(newRoleJson, JsonRole.class);

	}

	protected static void addUserToRole(JsonRole jsonRole,
			JsonResourceStatus jsonUser) throws Exception {

		RoleUpdate roleUpdate = new RoleUpdate();

		roleUpdate.setName(jsonRole.getName());
		roleUpdate.setId(jsonRole.getId());
		Long[] roleUsers = { jsonUser.getResource().getId() };
		roleUpdate.setUsers(roleUsers);
		roleUpdate.setGroups(new Long[0]);
		Long[] permissions = new Long[jsonRole.getPermissions().length];
		for (int x = 0; x < permissions.length; x++) {
			permissions[x] = jsonRole.getPermissions()[x].getId();
		}
		roleUpdate.setPermissions(permissions);

		String addUserToRoledJson = doPostJson("/hypersocket/api/roles/role",
				roleUpdate);
		debugJSON(addUserToRoledJson);

	}

	protected static void addUsersToRole(JsonRole jsonRole, Long[] users)
			throws Exception {

		RoleUpdate roleUpdate = new RoleUpdate();

		roleUpdate.setName(jsonRole.getName());
		roleUpdate.setId(jsonRole.getId());
		roleUpdate.setUsers(users);
		roleUpdate.setGroups(new Long[0]);
		Long[] permissions = new Long[jsonRole.getPermissions().length];
		for (int x = 0; x < permissions.length; x++) {
			permissions[x] = jsonRole.getPermissions()[x].getId();
		}
		roleUpdate.setPermissions(permissions);

		String addUserToRoledJson = doPostJson("/hypersocket/api/roles/role",
				roleUpdate);
		debugJSON(addUserToRoledJson);

	}

	protected static void addGroupToRole(JsonRole jsonRole,
			JsonResourceStatus jsonGroup) throws Exception {

		RoleUpdate roleUpdate = new RoleUpdate();

		roleUpdate.setName(jsonRole.getName());
		roleUpdate.setId(jsonRole.getId());
		Long[] roleGroups = { jsonGroup.getResource().getId() };
		roleUpdate.setGroups(roleGroups);
		roleUpdate.setUsers(new Long[0]);
		Long[] permissions = new Long[jsonRole.getPermissions().length];
		for (int x = 0; x < permissions.length; x++) {
			permissions[x] = jsonRole.getPermissions()[x].getId();
		}
		roleUpdate.setPermissions(permissions);

		String addGroupToRoledJson = doPostJson("/hypersocket/api/roles/role",
				roleUpdate);
		debugJSON(addGroupToRoledJson);

	}

	protected static JsonResourceStatus createRealm(String realmName)
			throws Exception {
		RealmUpdate realm = new RealmUpdate();
		realm.setName(realmName);
		realm.setProperties(new PropertyItem[0]);
		realm.setType("local");

		JsonResourceStatus json = getMapper().readValue(
				doPostJson("/hypersocket/api/realms/realm", realm),
				JsonResourceStatus.class);
		return json;

	}

	protected static void logOnNewUser(String[] strPermissions)
			throws Exception {
		logon("System", "admin", "Password123?");
		Long[] permissions = new Long[strPermissions.length];
		for (int x = 0; x < permissions.length; x++) {
			permissions[x] = getPermissionId(strPermissions[x]);
		}
		JsonResourceStatus jsonCreateUser = createUser("System", "user",
				"user", false);
		changePassword("user", jsonCreateUser);
		JsonRoleResourceStatus jsonCreateRole = createRole("newRole",
				permissions);
		addUserToRole(jsonCreateRole.getResource(), jsonCreateUser);
		resetEveryonePermission();
		logoff();
		logon("System", "user", "user");

	}

	@SuppressWarnings("unused")
	protected static void resetEveryonePermission() throws Exception {
		JsonRole jsonRole = getRole("Everyone");
		RoleUpdate role = new RoleUpdate();
		role.setId(jsonRole.getId());
		role.setName("Everyone");
		role.setPermissions(new Long[0]);
		role.setUsers(new Long[0]);
		role.setGroups(new Long[0]);

		String newRoleJson = doPostJson("/hypersocket/api/roles/role", role);
		JsonRoleResourceStatus newRoleJsonResourceStatus = mapper.readValue(
				newRoleJson, JsonRoleResourceStatus.class);
		debugJSON(newRoleJson);
	}

	public static JsonSession getSession() {
		return session;
	}

	public static JsonSession getAuxSession() {
		return auxSession;
	}

	public static JsonRole getSystemAdminRole() {
		return systemAdminRole;
	}

	public static ObjectMapper getMapper() {
		return mapper;
	}

}
