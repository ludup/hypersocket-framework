package com.hypersocket.tests.json.certificates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.security.Permission;
import java.security.PermissionCollection;
import java.util.Map;

import org.apache.http.message.BasicNameValuePair;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.hypersocket.certificates.CertificateType;
import com.hypersocket.json.JsonCertificate;
import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.json.JsonResponse;
import com.hypersocket.properties.json.PropertyItem;
import com.hypersocket.resource.ResourceUpdate;
import com.hypersocket.tests.AbstractServerTest;
import com.hypersocket.tests.json.utils.MultipartObject;
import com.hypersocket.tests.json.utils.PropertyObject;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public abstract class AbstractPermissionsTest extends AbstractServerTest {
	static long resourceID = 0;

	@BeforeClass
	public static void init() throws Exception {
		removeCryptographyRestrictions();
	}

	@Test
	public void test01_createCertificate() throws Exception {
		ResourceUpdate update = new ResourceUpdate();
		update.setName("TestCertificate");
		PropertyItem[] properties = new PropertyItem[7];
		properties[0] = new PropertyItem();
		properties[0].setId("commonName");
		properties[0].setValue("common name");
		properties[1] = new PropertyItem();
		properties[1].setId("organizationalUnit");
		properties[1].setValue("unit1");
		properties[2] = new PropertyItem();
		properties[2].setId("organization");
		properties[2].setValue("organization");
		properties[3] = new PropertyItem();
		properties[3].setId("location");
		properties[3].setValue("unknown");
		properties[4] = new PropertyItem();
		properties[4].setId("state");
		properties[4].setValue("State");
		properties[5] = new PropertyItem();
		properties[5].setId("country");
		properties[5].setValue("AF");
		properties[6] = new PropertyItem();
		properties[6].setId("certType");
		properties[6].setValue(CertificateType.RSA_1024.name());

		update.setProperties(properties);
		String result = doPostJson("/hypersocket/api/certificates/certificate",
				update);
		debugJSON(result);
		JsonResourceStatus json = getMapper().readValue(result,
				JsonResourceStatus.class);
		assertTrue(json.isSuccess());
		assertEquals("TestCertificate", json.getResource().getName());
		resourceID = json.getResource().getId();
	}

	@Test
	public void test02_getCertificate() throws Exception {
		assertNotEquals(0, resourceID);
		String result = doGet("/hypersocket/api/certificates/certificate/"
				+ resourceID);
		debugJSON(result);
		JsonCertificate json = getMapper().readValue(result,
				JsonCertificate.class);
		assertEquals("TestCertificate", json.getName());
	}

	@Test
	public void test03_updateCertificate() throws Exception {
		assertNotEquals(0, resourceID);
		ResourceUpdate update = new ResourceUpdate();
		update.setName("TestCertificate");
		update.setId(resourceID);
		PropertyItem[] properties = new PropertyItem[7];
		properties[0] = new PropertyItem();
		properties[0].setId("commonName");
		properties[0].setValue("common name");
		properties[1] = new PropertyItem();
		properties[1].setId("organizationalUnit");
		properties[1].setValue("unit1");
		properties[2] = new PropertyItem();
		properties[2].setId("organization");
		properties[2].setValue("organization");
		properties[3] = new PropertyItem();
		properties[3].setId("location");
		properties[3].setValue("unknown");
		properties[4] = new PropertyItem();
		properties[4].setId("state");
		properties[4].setValue("Nottingham");
		properties[5] = new PropertyItem();
		properties[5].setId("country");
		properties[5].setValue("UK");
		properties[6] = new PropertyItem();
		properties[6].setId("certType");
		properties[6].setValue(CertificateType.RSA_1024.name());
		update.setProperties(properties);
		String result = doPostJson("/hypersocket/api/certificates/certificate",
				update);
		debugJSON(result);
		JsonResponse json = getMapper().readValue(result, JsonResponse.class);
		assertTrue(json.isSuccess());
		assertEquals("Updated Certificate TestCertificate", json.getMessage());
	}

	@Test
	public void test04_testDownloadCSR() throws Exception {
		assertNotEquals(0, resourceID);
		String result = doGet("/hypersocket/api/certificates/downloadCSR/"
				+ resourceID);
		assertTrue(result.startsWith("-----BEGIN CERTIFICATE REQUEST-----"));
	}

	@Test
	public void test05_testDownloadCertificate() throws Exception {
		assertNotEquals(0, resourceID);
		String result = doGet("/hypersocket/api/certificates/downloadCertificate/"
				+ resourceID);
		assertTrue(result.startsWith("-----BEGIN CERTIFICATE-----"));
	}

	@Test
	public void test06_testExportPFX() throws Exception {
		assertNotEquals(0, resourceID);
		JsonResponse json = getMapper().readValue(
				doPost("/hypersocket/api/certificates/exportPfx/" + resourceID,
						new BasicNameValuePair("passphrase", "passsword")),
				JsonResponse.class);
		assertTrue(json.isSuccess());
		doGet("/hypersocket/api/certificates/downloadPfx/" + resourceID);
	}

	@Test
	public void test07_testExportPEM() throws Exception {
		assertNotEquals(0, resourceID);
		JsonResponse json = getMapper().readValue(
				doPost("/hypersocket/api/certificates/exportPem/" + resourceID,
						new BasicNameValuePair("passphrase", "passsword")),
				JsonResponse.class);
		assertTrue(json.isSuccess());
		doGet("/hypersocket/api/certificates/downloadPem/" + resourceID);
	}

	@Test
	public void test08_testUploadPEM() throws Exception {
		assertNotEquals(0, resourceID);
		File file = new File(certificatesFolder.getAbsoluteFile()
				+ File.separator + "certificate.pem");
		MultipartObject filePro = new MultipartObject("file", file);
		File bundleFile = new File(certificatesFolder.getAbsoluteFile()
				+ File.separator + "ca_bundle.pem");
		MultipartObject fileBundle = new MultipartObject("bundle", bundleFile);
		File keyFile = new File(certificatesFolder.getAbsoluteFile()
				+ File.separator + "key.pem");
		MultipartObject Key = new MultipartObject("key", keyFile);
		PropertyObject passphrase = new PropertyObject("passphrase", "");

		PropertyObject[] properties = new PropertyObject[] { passphrase };
		String result = doPostMultiparts("/hypersocket/api/certificates/pem/"
				+ resourceID, properties, filePro, fileBundle, Key);
		debugJSON(result);
		JsonResponse json = getMapper().readValue(result, JsonResponse.class);
		assertTrue(json.isSuccess());
	}

	@Test
	public void test09_testUploadPFX() throws Exception {
		File file = new File(certificatesFolder.getAbsoluteFile()
				+ File.separator + "cert.pfx");
		MultipartObject pfxFile = new MultipartObject("key", file);
		PropertyObject passphrase = new PropertyObject("passphrase", "");

		PropertyObject[] properties = new PropertyObject[] { passphrase };
		String result = doPostMultiparts("/hypersocket/api/certificates/pfx/"
				+ resourceID, properties, pfxFile);
		debugJSON(result);
		JsonResponse json = getMapper().readValue(result, JsonResponse.class);
		assertTrue(json.isSuccess());
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

}
