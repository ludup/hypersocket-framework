package com.hypersocket.json;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class JsonClient {

	BasicCookieStore cookieStore = new BasicCookieStore();
	protected ObjectMapper mapper = new ObjectMapper();
	protected JsonSession session;

	String hostname;
	int port;
	String path;
	HttpClient client;
	
	public JsonClient(String hostname, int port, String path) {
		this.hostname = hostname;
		this.port = port;
		this.path = path;
		this.client = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
	}
	


	public void logon(String username, String password)
			throws Exception {
		logon(username, password, false, null);
	}

	public void logon(String username, String password,
			boolean expectChangePassword, String newPassword) throws Exception {

		String json = doPost("api/logon/basic");
		debugJSON(json);

		AuthenticationResult result = mapper.readValue(json,
				AuthenticationResult.class);

		AuthenticationRequiredResult resultWithForm = mapper.readValue(json,
				AuthenticationRequiredResult.class);

		/**
		 * TODO attach to callback for form authentication
		 */
		String logonJson = doPost("api/logon",
				new BasicNameValuePair("username", username),
				new BasicNameValuePair("password", password));

		debugJSON(logonJson);

		AuthenticationResult logonResult = mapper.readValue(logonJson,
				AuthenticationResult.class);
		if (logonResult.getSuccess()) {
			JsonLogonResult logon = mapper.readValue(logonJson,
					JsonLogonResult.class);
			session = logon.getSession();
		} else {
			session = null;
		}
	}

	public String debugJSON(String json) throws JsonParseException,
			JsonMappingException, IOException {
		Object obj = mapper.readValue(json, Object.class);
		String ret = mapper.writerWithDefaultPrettyPrinter()
				.writeValueAsString(obj);
		System.out.println(ret);
		return ret;

	}

	public void logoff() throws JsonParseException,
			JsonMappingException, IOException {

		doGet("api/logoff");
		session = null;
	}

	public String doPost(String url, NameValuePair... postVariables)
			throws URISyntaxException, ClientProtocolException, IOException {

		if (!url.startsWith("/")) {
			url = "/" + url;
		}

		HttpUriRequest login = RequestBuilder
				.post()
				.addHeader("X-Csrf-Token", session==null ? "<unknown>" : session.getCsrfToken())
				.setUri(new URI(String.format("https://%s:%d%s%s", hostname, port, path, url)))
				.addParameters(postVariables).build();

		System.out.println("Executing request " + login.getRequestLine());

		CloseableHttpClient httpClient = (CloseableHttpClient) client;
		CloseableHttpResponse response = (CloseableHttpResponse) httpClient
				.execute(login);

		System.out.println("Response: " + response.getStatusLine().toString());

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new ClientProtocolException(
					"Expected status code 200 for doPost");
		}

		try {
			return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		} finally {
			response.close();
		}

	}

	public String doGet(String url) throws ClientProtocolException,
			IOException {

		if (!url.startsWith("/")) {
			url = "/" + url;
		}

		HttpGet httpget = new HttpGet(String.format("https://%s:%d%s%s", hostname, port, path, url));
		httpget.addHeader("X-Csrf-Token", session==null ? "<unknown>" : session.getCsrfToken());
		System.out.println("Executing request " + httpget.getRequestLine());

		CloseableHttpClient httpClient = (CloseableHttpClient) client;
		CloseableHttpResponse response = (CloseableHttpResponse) httpClient
				.execute(httpget);

		System.out.println("Response: " + response.getStatusLine().toString());

		try {
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new ClientProtocolException(
						"Expected status code 200 for doGet ["
								+ response.getStatusLine().getStatusCode()
								+ "]");
			}

			return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		} finally {
			response.close();
		}
	}

	public String doDelete(String url)
			throws ClientProtocolException, IOException {

		if (!url.startsWith("/")) {
			url = "/" + url;
		}

		HttpDelete httpdelete = new HttpDelete(String.format("https://%s:%d%s%s", hostname, port, path, url));
		httpdelete.addHeader("X-Csrf-Token", session==null ? "<unknown>" : session.getCsrfToken());
		System.out.println("Executing request " + httpdelete.getRequestLine());

		CloseableHttpClient httpClient = (CloseableHttpClient) client;
		CloseableHttpResponse response = (CloseableHttpResponse) httpClient
				.execute(httpdelete);

		System.out.println("Response: " + response.getStatusLine().toString());

		try {
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new ClientProtocolException(
						"Expected status code 200 for doGet ["
								+ response.getStatusLine().getStatusCode()
								+ "]");
			}

			return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		} finally {
			response.close();
		}
	}

	public String doPostMultiparts(String url,
			PropertyObject[] properties, MultipartObject... files)
			throws ClientProtocolException, IOException {
		if (!url.startsWith("/")) {
			url = "/" + url;
		}
		HttpPost postMethod = new HttpPost(String.format("https://%s:%d%s%s", hostname, port, path, url));
		postMethod.addHeader("X-Csrf-Token", session==null ? "<unknown>" : session.getCsrfToken());
		
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		if (properties != null && properties.length > 0) {
			for (PropertyObject property : properties) {
				/**
				 * I've changed this because of deprecation warning. This may
				 * break the test.
				 */
				builder.addPart(property.getProertyName(), new StringBody(
						property.getPropertyValue(),
						ContentType.APPLICATION_FORM_URLENCODED));
			}
		}
		for (MultipartObject file : files) {
			builder.addPart(file.getProperty(), new FileBody(file.getFile()));
		}
		HttpEntity reqEntity = builder.build();
		postMethod.setEntity(reqEntity);
		System.out.println("Executing request " + postMethod.getRequestLine());
		CloseableHttpClient httpClient = (CloseableHttpClient) client;
		CloseableHttpResponse response = (CloseableHttpResponse) httpClient
				.execute(postMethod);

		System.out.println("Response: " + response.getStatusLine().toString());

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new ClientProtocolException(
					"Expected status code 200 for doPost");
		}

		try {
			return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		} finally {
			response.close();
		}
	}

	public <T> T doGet(String url, Class<T> clz)
			throws ClientProtocolException, IOException {
		
		String json = doGet(url);
		
		debugJSON(json);
		
		return mapper.readValue(json, clz);
	}

	public String doPost(String url, String json)
			throws URISyntaxException, ClientProtocolException, IOException {

		if (!url.startsWith("/")) {
			url = "/" + url;
		}

		HttpPost postMethod = new HttpPost(String.format("https://%s:%d%s%s", hostname, port, path, url));
		postMethod.addHeader("X-Csrf-Token", session==null ? "<unknown>" : session.getCsrfToken());
		
		StringEntity se = new StringEntity("JSON: " + json.toString());
		se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
				"application/json"));

		postMethod.setEntity(se);

		CloseableHttpClient httpClient = (CloseableHttpClient) client;
		CloseableHttpResponse response = (CloseableHttpResponse) httpClient
				.execute(postMethod);

		System.out.println("Response: " + response.getStatusLine().toString());

		if (response.getStatusLine().getStatusCode() != 200) {
			throw new ClientProtocolException(
					"Expected status code 200 for doPost");
		}

		try {
			return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		} finally {
			response.close();
		}

	}

	public String doPostJson(String url, Object jsonObject)
			throws URISyntaxException, IllegalStateException, IOException {

		if (!url.startsWith("/")) {
			url = "/" + url;
		}

		String json = mapper.writeValueAsString(jsonObject);

		StringEntity requestEntity = new StringEntity(json,
				ContentType.APPLICATION_JSON);

		HttpUriRequest request = RequestBuilder
				.post()
				.addHeader("X-Csrf-Token", session==null ? "<unknown>" : session.getCsrfToken())
				.setUri(new URI(String.format("https://%s:%d%s%s", hostname, port, path, url)))
				.setEntity(requestEntity).build();

		System.out.println("Executing request " + request.getRequestLine());

		CloseableHttpClient httpClient = (CloseableHttpClient) client;
		CloseableHttpResponse response = (CloseableHttpResponse) httpClient
				.execute(request);

		System.out.println("Response: " + response.getStatusLine().toString());

		if (response.getStatusLine().getStatusCode() != 200) {

			throw new ClientProtocolException(
					"Expected status code 200 for doPost");
		}
		try {
			return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		} finally {
			response.close();
		}
	}

	public JsonSession getSession() {
		return session;
	}

	public ObjectMapper getMapper() {
		return mapper;
	}

}
