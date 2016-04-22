package com.hypersocket.tests.json.permissions;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.hypersocket.tests.AbstractServerTest;

public class UnauthorizedTest extends AbstractServerTest {

	
	@Test(expected=ClientProtocolException.class)
	public void accessPermissionListwithoutLogin() throws Exception{
		doGet("/hypersocket/api/permissions/list");
    }
	
	@Test(expected=ClientProtocolException.class)
	public void accessPermissionWithoutLogin() throws Exception{
		doGet("/hypersocket/api/permissions/permission/config.read/");
    }
}
