package com.hypersocket.http;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

public class HttpUtils {

	static BasicCookieStore cookieStore;
	
	public static CloseableHttpClient createHttpClient(boolean allowSelfSigned)
			throws IOException {
		CloseableHttpClient httpclient = null;

		if(allowSelfSigned) {
		try {
			SSLContextBuilder builder = new SSLContextBuilder();
			builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
			SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
					builder.build(),
					SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
			httpclient = HttpClients.custom()
					.setSSLSocketFactory(sslsf)
					.setDefaultCookieStore(cookieStore).build();
		} catch (Exception e) {
			throw new IOException(e);
		}
		
		} else {
			httpclient = HttpClients.custom()
					.setDefaultCookieStore(cookieStore).build();
		}
		
		return httpclient;
	}
	
	public static String doHttpPost(String url, Map<String,String> parameters, boolean allowSelfSigned) throws IOException {
		
		CloseableHttpClient client = createHttpClient(allowSelfSigned);
		
		try {
			HttpPost request = new HttpPost(url);
			
			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			
			for(String name : parameters.keySet()) {
				nameValuePairs.add(new BasicNameValuePair(name, parameters.get(name)));
			}
			request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(request);
			
			if(response.getStatusLine().getStatusCode()!=HttpStatus.SC_OK) {
				throw new IOException("Received " + response.getStatusLine().toString());
			}
			HttpEntity entity = response.getEntity();
			return EntityUtils.toString(entity);
		
		} finally {
			try {
				client.close();
			} catch (IOException e) {
			}
		}
	}
}
