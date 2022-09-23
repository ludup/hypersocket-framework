package com.hypersocket.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;

public class HttpUtilsHolder implements HttpUtils {

	private static HttpUtils instance = new HttpUtilsApacheImpl();

	public static HttpUtils getInstance() {
		return instance;
	}
	
	public static void setInstance(HttpUtils instance) {
		HttpUtilsHolder.instance = instance;
	}
	
	@Override
	public CloseableHttpClient createHttpClient(boolean allowSelfSigned) throws IOException {
		return instance.createHttpClient(allowSelfSigned);
	}

	@Override
	public String doHttpPost(String url, Map<String, String> parameters, 
			boolean allowSelfSigned)
					throws IOException {
		return instance.doHttpPost(url, parameters, allowSelfSigned);
	}
	
	@Override
	public String doHttpPost(String url, Map<String, String> parameters, boolean allowSelfSigned, Map<String,String> additionalHeaders) throws IOException {
		return instance.doHttpPost(url, parameters, allowSelfSigned, additionalHeaders);
	}
	
	@Override
	public String doHttpPost(String url, Map<String, String> parameters, boolean allowSelfSigned, Map<String,String> additionalHeaders, int... acceptableResponses) throws IOException {
		return instance.doHttpPost(url, parameters, allowSelfSigned, additionalHeaders, acceptableResponses);
	}

	@Override
	public InputStream doHttpGet(String uri, boolean allowSelfSigned) throws IOException {
		return instance.doHttpGet(uri, allowSelfSigned);
	}

	@Override
	public CloseableHttpResponse doHttpGet(String uri, boolean allowSelfSigned, Map<String, String> headers)
			throws IOException {
		return instance.doHttpGet(uri, allowSelfSigned, headers);
	}
	
	@Override
	public String doHttpGetContent(String uri, boolean allowSelfSigned, Map<String, String> headers)
			throws IOException {
		return instance.doHttpGetContent(uri, allowSelfSigned, headers);
	}

	@Override
	public void setVerifier(HostnameVerifier verifier) {
		instance.setVerifier(verifier);
	}

	@Override
	public CloseableHttpResponse execute(HttpUriRequest request, boolean allowSelfSigned) throws IOException {
		return instance.execute(request, allowSelfSigned);
	}

	@Override
	public InputStream doHttpGetInputStream(String uri, boolean allowSelfSigned, Map<String, String> headers)
			throws IOException {
		return instance.doHttpGetInputStream(uri, allowSelfSigned, headers);
	}
}
