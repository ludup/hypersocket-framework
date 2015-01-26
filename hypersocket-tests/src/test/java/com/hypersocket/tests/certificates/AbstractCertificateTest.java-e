package com.hypersocket.tests.certificates;

import java.io.File;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.message.BasicNameValuePair;
import org.junit.Test;

import com.hypersocket.tests.AbstractServerTest;
import com.hypersocket.tests.MultipartObject;
import com.hypersocket.tests.PropertyObject;

public class AbstractCertificateTest extends AbstractServerTest {
	
	@Test(expected=ClientProtocolException.class)
	public void testGetConfiguration() throws Exception{
		doGet("/hypersocket/api/certificates");
		
	}
	
	@Test(expected=ClientProtocolException.class)
	public void testKeyGeneration() throws Exception{
		doPost("/hypersocket/api/certificates/generateCSR", 
				            new BasicNameValuePair("cn","common name"),//common name
				            new BasicNameValuePair("ou", "unit1"), //Organizational unit
				            new BasicNameValuePair("o", "organization"), //Organization
                            new BasicNameValuePair("l", "location1"), //location
            			    new BasicNameValuePair("s", "State"), //State
            			    new BasicNameValuePair("c", "AF")); //country		
	}
	
	@Test(expected=ClientProtocolException.class)
	public void testDeleteCertificate() throws Exception{
		doDelete("/hypersocket/api/certificates");
	}
	
	@Test(expected=ClientProtocolException.class)
	public void testUploadKey()throws Exception{
		
		File file=new File(System.getProperty("user.dir")+File.separator+"key_files"+File.separator+"server.crt");
		MultipartObject filePro=new MultipartObject("file", file);
				
		File bundleFile=new File(System.getProperty("user.dir")+File.separator+"key_files"+File.separator+"ca_bundle.crt");
		MultipartObject fileBundle=new MultipartObject("bundle", bundleFile);
		
		
		File keyFile=new File(System.getProperty("user.dir")+File.separator+"key_files"+File.separator+"server.key");
		MultipartObject Key=new MultipartObject("key", keyFile);
		
		PropertyObject passphrase=new PropertyObject("passphrase", "");
		
		PropertyObject[] properties= new PropertyObject[]{passphrase};
		doPostMultiparts("/hypersocket/api/certificates/key", properties, filePro,fileBundle,Key);
	}
	
	@Test(expected=ClientProtocolException.class)
	public void testUploadPfx() throws Exception{
		
		File keyFile=new File(System.getProperty("user.dir")+File.separator+"key_files"+File.separator+"nervepoint-www-wildcard.pfx");
		MultipartObject filePro=new MultipartObject("key", keyFile);
		
        PropertyObject passphrase=new PropertyObject("passphrase", "bluemars73");
		PropertyObject[] properties= new PropertyObject[]{passphrase};
	   
		doPostMultiparts("/hypersocket/api/certificates/pfx", properties,filePro);
		
	
	}
	
	@Test(expected=ClientProtocolException.class)
	public void testUploadCertificate()throws Exception{
		File keyFile=new File(System.getProperty("user.dir")+File.separator+"key_files"+File.separator+"server.crt");
		MultipartObject filePro=new MultipartObject("file", keyFile);
		
		File BundleFile=new File(System.getProperty("user.dir")+File.separator+"key_files"+File.separator+"ca_bundle.crt");
		MultipartObject KeyBundle=new MultipartObject("bundle", BundleFile);
		doPostMultiparts("/hypersocket/api/certificates/cert",null,filePro,KeyBundle);
	}
}
