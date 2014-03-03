package com.hypersocket.tests;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

public class UnauthorizedTests extends AbstractServerTest {


	@Test(expected=ClientProtocolException.class)
	public void tryUnauthorizedUserList() throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/users");
	}
	
	@Test(expected=ClientProtocolException.class)
	public void tryUnauthorizedGroupList() throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/groups");
	}
	
	@Test(expected=ClientProtocolException.class)
	public void tryUnauthorizedRoleList() throws ClientProtocolException, IOException {
		doGet("/hypersocket/api/roles");
	}
}
