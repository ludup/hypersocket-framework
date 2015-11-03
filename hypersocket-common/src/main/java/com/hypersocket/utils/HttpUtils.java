package com.hypersocket.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;

public interface HttpUtils {

	CloseableHttpResponse doHttpGet(String uri, boolean allowSelfSigned, Map<String, String> headers)
			throws IOException;

	InputStream doHttpGet(String uri, boolean allowSelfSigned) throws IOException;

	String doHttpPost(String url, Map<String, String> parameters, boolean allowSelfSigned) throws IOException;

	CloseableHttpClient createHttpClient(boolean allowSelfSigned) throws IOException;

}
