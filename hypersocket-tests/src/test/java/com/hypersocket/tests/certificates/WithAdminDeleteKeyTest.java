package com.hypersocket.tests.certificates;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hypersocket.json.JsonResponse;
import com.hypersocket.tests.AbstractServerTest;

public class WithAdminDeleteKeyTest extends AbstractServerTest {

	@BeforeClass
	public static void init() throws Exception {
		logon("Default", "admin", "Password123?");
	}

	@Test
	public void testDeleteCertificate() throws Exception {
		String json = doDelete("/hypersocket/api/certificates");
		JsonResponse resp = getMapper().readValue(json, JsonResponse.class);
		assertTrue(resp.isSuccess());
		assertEquals(
				"Your keys & certificates were reset. You must restart the application.",
				resp.getMessage());
	}

	@AfterClass
	public static void finish() throws Exception {
		logoff();
	}

}
