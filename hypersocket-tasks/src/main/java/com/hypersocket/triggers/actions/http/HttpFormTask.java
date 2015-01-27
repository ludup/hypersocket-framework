package com.hypersocket.triggers.actions.http;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.triggers.TaskResult;
import com.hypersocket.triggers.TriggerResourceService;
import com.hypersocket.triggers.ValidationException;

@Component
public class HttpFormTask extends AbstractTaskProvider {

	static Logger log = LoggerFactory.getLogger(HttpFormTask.class);

	public static final String RESOURCE_BUNDLE = "HttpForm";

	public static final String RESOURCE_KEY = "httpForm";

	public static final String METHOD_GET = "GET";
	public static final String METHOD_POST = "POST";
	public static final String USER_AGENT = "Mozilla/5.0";

	static BasicCookieStore cookieStore;

	@Autowired
	HttpFormTaskRepository repository;

	@Autowired
	TriggerResourceService triggerService;

	@Autowired
	I18NService i18nService;

	@Autowired
	TaskProviderService taskService;

	@Autowired
	EventService eventService;
	
	@PostConstruct
	private void postConstruct() {

		i18nService.registerBundle(RESOURCE_BUNDLE);
		taskService.registerTaskProvider(this);
		
		eventService.registerEvent(HttpFormTaskResult.class, RESOURCE_BUNDLE);
	}

	@Override
	public String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public String[] getResourceKeys() {
		return new String[] { RESOURCE_KEY };
	}

	@Override
	public void validate(Task task, Map<String, String> parameters)
			throws ValidationException {
		if (parameters.containsKey("httpForm.url")) {
			throw new ValidationException("URL required");
		}
		if (parameters.containsKey("httpForm.responseList")) {
			throw new ValidationException("Correct response codes required");
		}
	}

	@Override
	public TaskResult execute(Task task, SystemEvent event)
			throws ValidationException {

		String method = repository.getValue(task, "httpForm.method");
		String url = repository.getValue(task, "httpForm.url");
		String[] variables = repository.getValues(task, "httpForm.variables");
		String[] responses = repository
				.getValues(task, "httpForm.responseList");
		boolean checkCertificate = repository.getBooleanValue(task,
				"httpForm.certificate");

		if (log.isInfoEnabled()) {
			log.info("Method " + method);
			log.info("URL " + url);
			log.info("variables " + variables.toString());
		}
		CloseableHttpClient client = HttpClients.custom()
				.setDefaultCookieStore(cookieStore).build();

		HttpResponse response;
		try {
			if (!checkCertificate) {
				client = createHttpClient();
			}
			if (METHOD_GET.equals(method)) {
				url = url + "?";
				for (int x = 0; x < variables.length; x++) {
					String variable = variables[x];
					url = url + variable;
					if (!((x + 1) == variables.length)) {
						url = url + "&";
					}
				}
				HttpGet request = new HttpGet(url);
				request.addHeader("User-Agent", USER_AGENT);
				response = client.execute(request);

			} else {
				HttpPost request = new HttpPost(url);
				
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(
						variables.length);
				for (String variable : variables) {
					String[] namePair = variable.split("=");
					nameValuePairs.add(new BasicNameValuePair(namePair[0],
							namePair[1]));
				}
				
				request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
				response = client.execute(request);

			}

			HttpEntity entity = response.getEntity();
			String content = EntityUtils.toString(entity);
			
			if (Arrays.asList(responses).contains(
					Integer.toString(response.getStatusLine().getStatusCode()))) {
				return new HttpFormTaskResult(this, event.getCurrentRealm(),
						task, method, url, variables, response.getStatusLine().getStatusCode(), content);
			} else {
				return new HttpFormTaskResult(this, new ClientProtocolException(
						"Status code " + response.getStatusLine().getStatusCode() + " not expected."), event.getCurrentRealm(),
						task, method, url, variables, response.getStatusLine().getStatusCode(), content);
			}

		} catch (Exception e) {
			log.error("Failed to fully process " + method + " method for "
					+ url + "variables: " + variables, e);
			return new HttpFormTaskResult(this, e, event.getCurrentRealm(),
					task, method, url, variables);
		}
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}

	public CloseableHttpClient createHttpClient()
			throws NoSuchAlgorithmException, KeyStoreException,
			KeyManagementException {
		CloseableHttpClient httpclient = null;

		SSLContextBuilder builder = new SSLContextBuilder();
		builder.loadTrustMaterial(null, new TrustSelfSignedStrategy());
		SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(
				builder.build(),
				SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
		httpclient = HttpClients.custom().setSSLSocketFactory(sslsf).build();
		return httpclient;
	}

}
