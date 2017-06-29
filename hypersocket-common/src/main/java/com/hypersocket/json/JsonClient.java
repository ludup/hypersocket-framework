package com.hypersocket.json;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
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
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.ServerInfo;
import com.hypersocket.utils.HttpUtilsHolder;
import com.hypersocket.utils.HypersocketUtils;


public class JsonClient {

	static Logger log = LoggerFactory.getLogger(JsonClient.class);
	
	protected ObjectMapper mapper = new ObjectMapper();
	protected JsonSession session;

	boolean debug = false;
	boolean allowSelfSigned = false;
	String hostname;
	int port;
	String path;
	String scheme = "basic";
	
	public JsonClient(String hostname, int port, String path) throws IOException {
		this.hostname = hostname;
		this.port = port;
		this.path = path;
	}
	
	public JsonClient(String hostname, int port) throws IOException {
		this(hostname, port, false);
	}
	
	public JsonClient(String hostname, int port, boolean allowSelfSigned) throws IOException {
		this.hostname = hostname;
		this.port = port;
		this.path = "/discover";
		
		setAllowSelfSignedCertificates(allowSelfSigned);
		try {
			
			if(log.isInfoEnabled()) {
				log.info("Discovering server path configuration");
			}
			
			String json = doGet("");
			ServerInfo info = mapper.readValue(json, ServerInfo.class);
			this.path = info.getBasePath();
			
			if(log.isInfoEnabled()) {
				log.info(String.format("Server application path is %s", this.path));
			}
			
		} catch (JsonStatusException | URISyntaxException e) {
			throw new IOException(e.getMessage(), e);
		}
	}
	
	public void logon(String username, String password) throws URISyntaxException, IOException, JsonStatusException {
		logon(username, password, false, null);
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
	}
	
	public void setAuthenticationScheme(String scheme) {
		this.scheme = scheme;
	}
	
	public void setAllowSelfSignedCertificates(boolean allowSelfSigned) {
		this.allowSelfSigned = allowSelfSigned;
	}
	
	public void logon(String username, String password,
			boolean expectChangePassword, String newPassword) throws URISyntaxException, IOException, JsonStatusException {

		String logonJson = doPost(String.format("api/logon/%s", scheme),
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
			throw new IOException("Authentication failed");
		}
	}

	public String debugJSON(String json) throws JsonParseException,
			JsonMappingException, IOException {
		if(debug) {
			Object obj = mapper.readValue(json, Object.class);
			String ret = mapper.writerWithDefaultPrettyPrinter()
					.writeValueAsString(obj);
			log.info(ret);
			return ret;
		}
		return json;
	}

	public void logoff() throws JsonParseException,
			JsonMappingException, JsonStatusException, IOException, URISyntaxException {

		doGet("api/logoff");
		session = null;
	}

	public <T> T doPost(String url, Class<T> clz, NameValuePair... postVariables)
			throws URISyntaxException, IOException, JsonStatusException {
		
		String json = doPost(url, postVariables);
		
		debugJSON(json);
		
		return mapper.readValue(json, clz);
	}
	
	public String doPost(String url, NameValuePair... postVariables)
			throws URISyntaxException, IOException, JsonStatusException {

		if (!url.startsWith("/")) {
			url = "/" + url;
		}

		url = HypersocketUtils.encodeURIPath(url);
		
		HttpUriRequest login = RequestBuilder
				.post()
				.addHeader("X-Csrf-Token", session==null ? "<unknown>" : session.getCsrfToken())
				.setUri(new URI(String.format("https://%s:%d%s%s", hostname, port, path, url)))
				.addParameters(postVariables).build();

		if(log.isInfoEnabled()) {
			log.info("Executing request " + login.getRequestLine());
		}
		
		CloseableHttpResponse response = HttpUtilsHolder.getInstance().execute(login, allowSelfSigned);

		if(log.isInfoEnabled()) {
			log.info("Response: " + response.getStatusLine().toString());
		}
		
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new JsonStatusException(response.getStatusLine().getStatusCode());
		}

		try {
			return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		} finally {
			response.close();
		}

	}

	public String doGet(String url) throws
			IOException, JsonStatusException, URISyntaxException {

		if (!url.startsWith("/")) {
			url = "/" + url;
		}

		url = HypersocketUtils.encodeURIPath(url);
		
		HttpGet httpget = new HttpGet(String.format("https://%s:%d%s%s", hostname, port, path, url));
		httpget.addHeader("X-Csrf-Token", session==null ? "<unknown>" : session.getCsrfToken());
		
		if(log.isInfoEnabled()) {
			log.info("Executing request " + httpget.getRequestLine());
		}
		
		CloseableHttpResponse response = HttpUtilsHolder.getInstance().execute(httpget, allowSelfSigned);

		if(log.isInfoEnabled()) {
			log.info("Response: " + response.getStatusLine().toString());
		}
		
		try {
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new JsonStatusException(response.getStatusLine().getStatusCode());
			}

			return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		} finally {
			response.close();
		}
	}

	public String doDelete(String url)
			throws IOException, JsonStatusException {

		if (!url.startsWith("/")) {
			url = "/" + url;
		}

		url = HypersocketUtils.encodeURIPath(url);
		
		HttpDelete httpdelete = new HttpDelete(String.format("https://%s:%d%s%s", hostname, port, path, url));
		httpdelete.addHeader("X-Csrf-Token", session==null ? "<unknown>" : session.getCsrfToken());
		
		if(log.isInfoEnabled()) {
			log.info("Executing request " + httpdelete.getRequestLine());
		}
		
		CloseableHttpResponse response = HttpUtilsHolder.getInstance().execute(httpdelete, allowSelfSigned);

		if(log.isInfoEnabled()) {
			log.info("Response: " + response.getStatusLine().toString());
		}
		
		try {
			if (response.getStatusLine().getStatusCode() != 200) {
				throw new JsonStatusException(response.getStatusLine().getStatusCode());
			}

			return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		} finally {
			response.close();
		}
	}

	public String doPostMultiparts(String url,
			PropertyObject[] properties, MultipartObject... files)
			throws IOException, JsonStatusException {
		
		if (!url.startsWith("/")) {
			url = "/" + url;
		}
		
		url = HypersocketUtils.encodeURIPath(url);
		
		HttpPost postMethod = new HttpPost(String.format("https://%s:%d%s%s", hostname, port, path, url));
		postMethod.addHeader("X-Csrf-Token", session==null ? "<unknown>" : session.getCsrfToken());
		
		MultipartEntityBuilder builder = MultipartEntityBuilder.create();
		if (properties != null && properties.length > 0) {
			for (PropertyObject property : properties) {
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
		
		if(log.isInfoEnabled()) {
			log.info("Executing request " + postMethod.getRequestLine());
		}
		
		CloseableHttpResponse response = HttpUtilsHolder.getInstance().execute(postMethod, allowSelfSigned);
		
		if(log.isInfoEnabled()) {
			log.info("Response: " + response.getStatusLine().toString());
		}
		
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new JsonStatusException(response.getStatusLine().getStatusCode());
		}

		try {
			return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		} finally {
			response.close();
		}
	}

	public <T> T doGet(String url, Class<T> clz)
			throws JsonStatusException, IOException, URISyntaxException {
		
		String json = doGet(url);
		
		debugJSON(json);
		
		return mapper.readValue(json, clz);
	}

	public String doPost(String url, String json)
			throws URISyntaxException, JsonStatusException, IOException {

		if (!url.startsWith("/")) {
			url = "/" + url;
		}

		url = HypersocketUtils.encodeURIPath(url);
		
		HttpPost postMethod = new HttpPost(String.format("https://%s:%d%s%s", hostname, port, path, url));
		postMethod.addHeader("X-Csrf-Token", session==null ? "<unknown>" : session.getCsrfToken());
		
		StringEntity se = new StringEntity("JSON: " + json.toString());
		se.setContentEncoding(new BasicHeader(HTTP.CONTENT_TYPE,
				"application/json"));

		postMethod.setEntity(se);

		if(log.isInfoEnabled()) {
			log.info("Executing request " + postMethod.getRequestLine());
		}
		
		CloseableHttpResponse response = HttpUtilsHolder.getInstance().execute(postMethod, allowSelfSigned);


		if(log.isInfoEnabled()) {
			log.info("Response: " + response.getStatusLine().toString());
		}
		
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new JsonStatusException(response.getStatusLine().getStatusCode());
		}

		try {
			return IOUtils.toString(response.getEntity().getContent(), "UTF-8");
		} finally {
			response.close();
		}

	}

	public String doPostJson(String url, Object jsonObject)
			throws URISyntaxException, IllegalStateException, IOException, JsonStatusException {

		if (!url.startsWith("/")) {
			url = "/" + url;
		}

		url = HypersocketUtils.encodeURIPath(url);
		
		String json = mapper.writeValueAsString(jsonObject);

		StringEntity requestEntity = new StringEntity(json,
				ContentType.APPLICATION_JSON);

		HttpUriRequest request = RequestBuilder
				.post()
				.addHeader("X-Csrf-Token", session==null ? "<unknown>" : session.getCsrfToken())
				.setUri(new URI(String.format("https://%s:%d%s%s", hostname, port, path, url)))
				.setEntity(requestEntity).build();
		
		if(log.isInfoEnabled()) {
			log.info("Executing request " + request.getRequestLine());
		}

		CloseableHttpResponse response = HttpUtilsHolder.getInstance().execute(request, allowSelfSigned);

		if(log.isInfoEnabled()) {
			log.info("Response: " + response.getStatusLine().toString());
		}
		
		if (response.getStatusLine().getStatusCode() != 200) {
			throw new JsonStatusException(response.getStatusLine().getStatusCode());
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

	public InputStream doGetInputStream(String url) throws UnsupportedOperationException, IOException, JsonStatusException {
		
		if (!url.startsWith("/")) {
			url = "/" + url;
		}

		url = HypersocketUtils.encodeURIPath(url);
		
		HttpGet httpget = new HttpGet(String.format("https://%s:%d%s%s", hostname, port, path, url));
		httpget.addHeader("X-Csrf-Token", session==null ? "<unknown>" : session.getCsrfToken());
		
		if(log.isInfoEnabled()) {
			log.info("Executing request " + httpget.getRequestLine());
		}
		
		return HttpUtilsHolder.getInstance().doHttpGet(httpget.getURI().toASCIIString(), allowSelfSigned);
	}
}
