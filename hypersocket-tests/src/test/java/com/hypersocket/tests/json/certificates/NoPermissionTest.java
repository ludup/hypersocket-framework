package com.hypersocket.tests.json.certificates;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.hypersocket.auth.AuthenticationPermission;
import com.hypersocket.certificates.CertificateType;
import com.hypersocket.json.JsonResourceStatus;
import com.hypersocket.json.JsonResponse;
import com.hypersocket.properties.json.PropertyItem;
import com.hypersocket.resource.ResourceUpdate;
import com.hypersocket.tests.AbstractServerTest;
import com.hypersocket.tests.json.utils.MultipartObject;
import com.hypersocket.tests.json.utils.PropertyObject;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class NoPermissionTest extends AbstractServerTest {
	static long certificateID;
	
	@BeforeClass
	public static void init() throws Exception {
		logon("System", "admin", "Password123?");
		//Create a certificate
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
		JsonResourceStatus json = getMapper().readValue(result,
				JsonResourceStatus.class);
		certificateID = json.getResource().getId();
		logoff();
		logOnNewUser(new String[] { AuthenticationPermission.LOGON
				.getResourceKey() });
	}

	@AfterClass
	public static void clean() throws Exception {
		logoff();
	}
	
	@Test(expected = ClientProtocolException.class)
	public void test01_createCertificate() throws Exception {
		ResourceUpdate update = new ResourceUpdate();
		update.setName("TestCertificate2");
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
		doPostJson("/hypersocket/api/certificates/certificate",
				update);
	}
	
	@Test(expected = ClientProtocolException.class)
	public void test02_getCertificate() throws Exception {
		doGet("/hypersocket/api/certificates/certificate/"+certificateID);
	}
	
	@Test(expected = ClientProtocolException.class)
	public void test03_updateCertificate() throws Exception {
		assertNotEquals(0, certificateID);
		ResourceUpdate update = new ResourceUpdate();
		update.setName("TestCertificate");
		update.setId(certificateID);
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
		doPostJson("/hypersocket/api/certificates/certificate",
				update);
	}

	@Test(expected = ClientProtocolException.class)
	public void test04_testDownloadCSR() throws Exception {
		assertNotEquals(0, certificateID);
		doGet("/hypersocket/api/certificates/downloadCSR/"
				+ certificateID);
	}

	@Test(expected = ClientProtocolException.class)
	public void test05_testDownloadCertificate() throws Exception {
		assertNotEquals(0, certificateID);
		doGet("/hypersocket/api/certificates/downloadCertificate/"
				+ certificateID);
	}

	@Test(expected = ClientProtocolException.class)
	public void test06_testExportPFX() throws Exception {
		assertNotEquals(0, certificateID);
		JsonResponse json = getMapper().readValue(
				doPost("/hypersocket/api/certificates/exportPfx/" + certificateID,
						new BasicNameValuePair("passphrase", "passsword")),
				JsonResponse.class);
		assertTrue(json.isSuccess());
		doGet("/hypersocket/api/certificates/downloadPfx/" + certificateID);
	}

	@Test(expected = ClientProtocolException.class)
	public void test07_testExportPEM() throws Exception {
		assertNotEquals(0, certificateID);
		JsonResponse json = getMapper().readValue(
				doPost("/hypersocket/api/certificates/exportPem/" + certificateID,
						new BasicNameValuePair("passphrase", "passsword")),
				JsonResponse.class);
		assertTrue(json.isSuccess());
		doGet("/hypersocket/api/certificates/downloadPem/" + certificateID);
	}

	@Test(expected = ClientProtocolException.class)
	public void test08_testUploadPEM() throws Exception {
		assertNotEquals(0, certificateID);
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
		doPostMultiparts("/hypersocket/api/certificates/pem/"
				+ certificateID, properties, filePro, fileBundle, Key);
	}

	@Test(expected = ClientProtocolException.class)
	public void test09_testUploadPFX() throws Exception {
		File file = new File(certificatesFolder.getAbsoluteFile()
				+ File.separator + "cert.pfx");
		MultipartObject pfxFile = new MultipartObject("key", file);
		PropertyObject passphrase = new PropertyObject("passphrase", "");

		PropertyObject[] properties = new PropertyObject[] { passphrase };
		doPostMultiparts("/hypersocket/api/certificates/pfx/"
				+ certificateID, properties, pfxFile);
		
	}

}
