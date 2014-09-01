package com.hypersocket.tests.certificates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Map;

import org.apache.http.message.BasicNameValuePair;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hypersocket.auth.AuthenticationPermission;
import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.json.JsonResponse;
import com.hypersocket.json.JsonRoleResourceStatus;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.tests.AbstractServerTest;
import com.hypersocket.tests.MultipartObject;
import com.hypersocket.tests.PropertyObject;

public class WithDelegatePermissionTest extends AbstractServerTest {
	@BeforeClass
	public static void init() throws Exception {
		logon("Default", "admin", "Password123?");
		JsonResourceStatus jsonCreateUser = createUser("Default", "user",
				"user", false);
		changePassword("user", jsonCreateUser);
		Long[] permissions = {
				getPermissionId(AuthenticationPermission.LOGON.getResourceKey()),
				getPermissionId(SystemPermission.SYSTEM.getResourceKey()) };
		JsonRoleResourceStatus jsonCreateRole = createRole("newrole",
				permissions);
		addUserToRole(jsonCreateRole.getResource(), jsonCreateUser);
		logoff();
		logon("Default", "user", "user");
		removeCryptographyRestrictions();
	}

	@Test
	public void testGetConfiguration() throws Exception {
		String json = doGet("/hypersocket/api/certificates");
		debugJSON(json);
		assertNotNull(json);
		JsonResponse resp = getMapper().readValue(json, JsonResponse.class);
		assertTrue(resp.isSuccess());
	}

	@Test
	public void testKeyGeneration() throws Exception {
		String json = doPost("/hypersocket/api/certificates/generateCSR",
				new BasicNameValuePair("cn", "common name"),// common name
				new BasicNameValuePair("ou", "unit1"), // Organizational unit
				new BasicNameValuePair("o", "organization"), // Organization
				new BasicNameValuePair("l", "location1"), // location
				new BasicNameValuePair("s", "State"), // State
				new BasicNameValuePair("c", "AF")); // country
		JsonResponse resp = getMapper().readValue(json, JsonResponse.class);
		debugJSON(json);
		assertTrue(resp.isSuccess());
		assertEquals(0,
				resp.getMessage()
						.indexOf("-----BEGIN CERTIFICATE REQUEST-----"));

	}

	@Test
	public void testUploadKey() throws Exception {

		File file = new File(System.getProperty("user.dir") + File.separator
				+ "key_files" + File.separator + "server.crt");
		MultipartObject filePro = new MultipartObject("file", file);

		File bundleFile = new File(System.getProperty("user.dir")
				+ File.separator + "key_files" + File.separator
				+ "ca_bundle.crt");
		MultipartObject fileBundle = new MultipartObject("bundle", bundleFile);

		File keyFile = new File(System.getProperty("user.dir") + File.separator
				+ "key_files" + File.separator + "server.key");
		MultipartObject Key = new MultipartObject("key", keyFile);

		PropertyObject passphrase = new PropertyObject("passphrase", "");

		PropertyObject[] properties = new PropertyObject[] { passphrase };
		String json = doPostMultiparts("/hypersocket/api/certificates/key",
				properties, filePro, fileBundle, Key);
		debugJSON(json);
		JsonResponse resp = getMapper().readValue(json, JsonResponse.class);
		assertTrue(resp.isSuccess());
		assertEquals(
				"Your key has been accepted. The server will need to be restarted.",
				resp.getMessage());
		json = doGet("/hypersocket/api/certificates");
		debugJSON(json);
	}

	@Test
	public void testUploadPfx() throws Exception {

		File keyFile = new File(System.getProperty("user.dir") + File.separator
				+ "key_files" + File.separator + "nervepoint-www-wildcard.pfx");
		MultipartObject filePro = new MultipartObject("key", keyFile);

		PropertyObject passphrase = new PropertyObject("passphrase",
				"bluemars73");
		PropertyObject[] properties = new PropertyObject[] { passphrase };

		String json = doPostMultiparts("/hypersocket/api/certificates/pfx",
				properties, filePro);
		debugJSON(json);
		JsonResponse resp = getMapper().readValue(json, JsonResponse.class);
		assertTrue(resp.isSuccess());
		assertEquals(
				"Your key has been accepted. The server will need to be restarted.",
				resp.getMessage());

	}

	@Test
	public void testUploadCertificate() throws Exception {

		File keyFile = new File(System.getProperty("user.dir") + File.separator
				+ "key_files" + File.separator + "server.crt");
		MultipartObject filePro = new MultipartObject("file", keyFile);

		File BundleFile = new File(System.getProperty("user.dir")
				+ File.separator + "key_files" + File.separator
				+ "ca_bundle.crt");
		MultipartObject KeyBundle = new MultipartObject("bundle", BundleFile);
		String json = doPostMultiparts("/hypersocket/api/certificates/cert",
				null, filePro, KeyBundle);
		debugJSON(json);

	}

	private static void removeCryptographyRestrictions() {
		if (!isRestrictedCryptography()) {
			return;
		}
		try {
			final Class<?> jceSecurity = Class
					.forName("javax.crypto.JceSecurity");
			final Class<?> cryptoPermissions = Class
					.forName("javax.crypto.CryptoPermissions");
			final Class<?> cryptoAllPermission = Class
					.forName("javax.crypto.CryptoAllPermission");

			final Field isRestrictedField = jceSecurity
					.getDeclaredField("isRestricted");
			isRestrictedField.setAccessible(true);
			isRestrictedField.set(null, false);

			final Field defaultPolicyField = jceSecurity
					.getDeclaredField("defaultPolicy");
			defaultPolicyField.setAccessible(true);
			final PermissionCollection defaultPolicy = (PermissionCollection) defaultPolicyField
					.get(null);

			final Field perms = cryptoPermissions.getDeclaredField("perms");
			perms.setAccessible(true);
			((Map<?, ?>) perms.get(defaultPolicy)).clear();

			final Field instance = cryptoAllPermission
					.getDeclaredField("INSTANCE");
			instance.setAccessible(true);
			defaultPolicy.add((Permission) instance.get(null));

		} catch (final Exception e) {

		}
	}

	private static boolean isRestrictedCryptography() {
		// This simply matches the Oracle JRE, but not OpenJDK.
		return "Java(TM) SE Runtime Environment".equals(System
				.getProperty("java.runtime.name"));
	}

	@AfterClass
	static public void clean() throws Exception {
		logoff();
	}
}
