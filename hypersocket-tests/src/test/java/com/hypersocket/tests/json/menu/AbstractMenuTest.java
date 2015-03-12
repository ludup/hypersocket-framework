package com.hypersocket.tests.json.menu;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import com.hypersocket.auth.AuthenticationPermission;
import com.hypersocket.certificates.CertificateResourcePermission;
import com.hypersocket.json.JsonRoleResourceStatus;
import com.hypersocket.tests.AbstractServerTest;

public abstract class AbstractMenuTest extends AbstractServerTest {

	static JsonRoleResourceStatus jsonCreateRole;

	@BeforeClass
	public static void init() throws Exception {
		logOnNewUser(new String[] {
				AuthenticationPermission.LOGON.getResourceKey(),
				CertificateResourcePermission.READ.getResourceKey() });
	}

	@AfterClass
	public static void clean() throws Exception {
		logoff();
	}
}
