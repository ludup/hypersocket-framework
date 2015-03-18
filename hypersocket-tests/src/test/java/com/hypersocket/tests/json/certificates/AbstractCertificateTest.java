package com.hypersocket.tests.json.certificates;

import java.io.File;
import java.security.KeyFactory;

import org.junit.BeforeClass;

import com.hypersocket.certs.X509CertificateUtils;
import com.hypersocket.tests.AbstractServerTest;

public abstract class AbstractCertificateTest extends AbstractServerTest {
	
	static File certicateFolder;
	@BeforeClass
	public static void init() throws Exception{
		File certicateFolder = new File(tmp, "cert_folder");
		certicateFolder.mkdirs();
		
	}

}
