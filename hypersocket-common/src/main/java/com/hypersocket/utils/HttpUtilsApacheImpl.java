package com.hypersocket.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

public class HttpUtilsApacheImpl implements HttpUtils {

	static BasicCookieStore cookieStore = new BasicCookieStore();

	@Override
	public CloseableHttpClient createHttpClient(boolean allowSelfSigned) throws IOException {
	
		CloseableHttpClient httpclient = null;
		if (allowSelfSigned) {
			try {
				SSLContextBuilder builder = new SSLContextBuilder();
				builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
				SSLConnectionSocketFactory cm = new SSLConnectionSocketFactory(builder.build());
				httpclient = HttpClients.custom().setSSLSocketFactory(cm).setDefaultCookieStore(cookieStore)
						.build();
			} catch (Exception e) {
				throw new IOException(e.getMessage(), e);
			}

		} else {
			httpclient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
		}
		return httpclient;
	
	}

	@Override
	public String doHttpPost(String url, Map<String, String> parameters, 
			boolean allowSelfSigned)
					throws IOException {
		return doHttpPost(url, parameters, allowSelfSigned, null);
	}
	
	@Override
	public String doHttpPost(String url, Map<String, String> parameters, boolean allowSelfSigned, Map<String,String> additionalHeaders) throws IOException {

		CloseableHttpClient client = createHttpClient(allowSelfSigned);

		try {
			HttpPost request = new HttpPost(url);

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

			for (String name : parameters.keySet()) {
				nameValuePairs.add(new BasicNameValuePair(name, parameters.get(name)));
			}
			if(additionalHeaders!=null) {
				for(Map.Entry<String, String> entry : additionalHeaders.entrySet()) {
					request.addHeader(entry.getKey(), entry.getValue());
				}
			}
			request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			HttpResponse response = client.execute(request);

			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
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

	@Override
	public InputStream doHttpGet(String uri, boolean allowSelfSigned) throws IOException {
		CloseableHttpClient client = createHttpClient(allowSelfSigned);
		HttpGet request = new HttpGet(uri);
		return new ContentInputStream(client, client.execute(request).getEntity().getContent());
	}

	@Override
	public CloseableHttpResponse doHttpGet(String uri, boolean allowSelfSigned, Map<String, String> headers)
			throws IOException {

		CloseableHttpClient client = createHttpClient(allowSelfSigned);
		HttpGet request = new HttpGet(uri);
		for (String key : headers.keySet()) {
			request.setHeader(key, headers.get(key));
		}
		return client.execute(request);
		
	}
	
	@Override
	public String doHttpGetContent(String uri, boolean allowSelfSigned, Map<String, String> headers)
			throws IOException {
		CloseableHttpResponse response = doHttpGet(uri, allowSelfSigned, headers);
		try {
			if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
				throw new IOException("Received " + response.getStatusLine().toString());
			}
			HttpEntity entity = response.getEntity();
			return EntityUtils.toString(entity);

		} finally {
			try {
				response.close();
			} catch (IOException e) {
			}
		}
	}

	static class ContentInputStream extends InputStream {

		CloseableHttpClient client;
		InputStream wrapped;

		ContentInputStream(CloseableHttpClient client, InputStream wrapped) {
			this.client = client;
			this.wrapped = wrapped;
		}

		@Override
		public int read() throws IOException {
			int b = wrapped.read();
			if (b == -1) {
				client.close();
			}
			return b & 0xFF;
		}

		@Override
		public int read(byte[] buf, int off, int len) throws IOException {
			int b = wrapped.read(buf, off, len);
			if (b == -1) {
				client.close();
			}
			return b;
		}

	}

	@Override
	public CloseableHttpResponse execute(HttpUriRequest request, boolean allowSelfSigned) throws IOException {
		CloseableHttpClient client = createHttpClient(allowSelfSigned);
		return client.execute(request);
	}
	
	@Override
	public void setVerifier(HostnameVerifier verifier) {
		
	}

}
