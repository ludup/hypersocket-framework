package com.hypersocket.tests.menu;

import org.apache.http.client.ClientProtocolException;
import org.junit.Test;

import com.hypersocket.tests.AbstractServerTest;

public class UnauthorizedTest extends AbstractServerTest {

	
	@Test(expected=ClientProtocolException.class)
	public void testCallMenuwithoutLogon() throws Exception{
		doGet("/hypersocket/api/menus");
	}
	
	@Test(expected=ClientProtocolException.class)
	public void testTableActions() throws Exception{
		doGet("/hypersocket/api/menus/tableActions/userActions");
	}
}
