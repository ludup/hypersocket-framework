package com.hypersocket.dictionary;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpCookie;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.dictionary.events.DictionaryResourceCreatedEvent;
import com.hypersocket.dictionary.events.DictionaryResourceDeletedEvent;
import com.hypersocket.dictionary.events.DictionaryResourceEvent;
import com.hypersocket.dictionary.events.DictionaryResourceUpdatedEvent;
import com.hypersocket.events.EventService;
import com.hypersocket.http.HttpUtilsImpl;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.resource.ResourcePassthroughException;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.transactions.TransactionService;
import com.hypersocket.upgrade.UpgradeService;
import com.hypersocket.upgrade.UpgradeServiceListener;

@Service
public class DictionaryResourceServiceImpl extends AbstractAuthenticatedServiceImpl
		implements DictionaryResourceService, UpgradeServiceListener {

	public static final String RESOURCE_BUNDLE = "DictionaryResourceService";
	public static final String USER_AGENT = "Mozilla/5.0";
	public static final String METHOD_GET = "GET";
	public static final String METHOD_POST = "POST";

	static Logger log = LoggerFactory.getLogger(DictionaryResourceServiceImpl.class);

	@Autowired
	private DictionaryResourceRepository dictionaryRepository;

	@Autowired
	private TransactionService transactionService;

	@Autowired
	private DictionaryResourceRepository repository;

	@Autowired
	private I18NService i18nService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private EventService eventService;

	@Autowired
	private UpgradeService upgradeService;

	@Autowired
	private HttpUtilsImpl httpUtils;

	@Autowired
	private SystemConfigurationService systemConfigurationService;

	@PostConstruct
	private void postConstruct() {

		i18nService.registerBundle(RESOURCE_BUNDLE);

		PermissionCategory cat = permissionService.registerPermissionCategory(RESOURCE_BUNDLE, "category.dictionarys");

		for (DictionaryResourcePermission p : DictionaryResourcePermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		repository.loadPropertyTemplates("dictionaryResourceTemplate.xml");

		eventService.registerEvent(DictionaryResourceEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(DictionaryResourceCreatedEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(DictionaryResourceUpdatedEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(DictionaryResourceDeletedEvent.class, RESOURCE_BUNDLE);

		EntityResourcePropertyStore.registerResourceService(Word.class, repository);
		upgradeService.registerListener(this);
	}

	protected Class<Word> getResourceClass() {
		return Word.class;
	}

	protected void fireResourceCreationEvent(Word resource) {
		eventService.publishEvent(new DictionaryResourceCreatedEvent(this, getCurrentSession(), resource));
	}

	protected void fireResourceCreationEvent(Word resource, Throwable t) {
		eventService.publishEvent(new DictionaryResourceCreatedEvent(this, resource, t, getCurrentSession()));
	}

	protected void fireResourceUpdateEvent(Word resource) {
		eventService.publishEvent(new DictionaryResourceUpdatedEvent(this, getCurrentSession(), resource));
	}

	protected void fireResourceUpdateEvent(Word resource, Throwable t) {
		eventService.publishEvent(new DictionaryResourceUpdatedEvent(this, resource, t, getCurrentSession()));
	}

	protected void fireResourceDeletionEvent(Word resource) {
		eventService.publishEvent(new DictionaryResourceDeletedEvent(this, getCurrentSession(), resource));
	}

	protected void fireResourceDeletionEvent(Word resource, Throwable t) {
		eventService.publishEvent(new DictionaryResourceDeletedEvent(this, resource, t, getCurrentSession()));
	}

	@Override
	public Word updateResource(Word resource, Locale locale, String text)
			throws ResourceException, AccessDeniedException {

		resource.setText(text);
		resource.setLocale(locale);
		repository.saveResource(resource);
		try {
			repository.saveResource(resource);
			fireResourceUpdateEvent(resource);
		} catch (Throwable t) {
			log.error("Failed to update resource", t);
			ResourcePassthroughException.maybeRethrow(t);
			fireResourceUpdateEvent(resource, t);
			if (t instanceof ResourceException) {
				throw (ResourceException) t;
			} else {
				throw new ResourceCreationException(AbstractResourceServiceImpl.RESOURCE_BUNDLE_DEFAULT,
						"generic.create.error", t.getMessage(), t);
			}
		}
		return resource;
	}

	@Override
	public Word createResource(Locale locale, String word) throws ResourceException, AccessDeniedException {

		Word resource = new Word();
		resource.setText(word);
		resource.setLocale(locale);

		assertPermission(DictionaryResourcePermission.CREATE);

		try {
			repository.saveResource(resource);
			fireResourceCreationEvent(resource);
		} catch (Throwable t) {
			log.error("Failed to create resource", t);
			ResourcePassthroughException.maybeRethrow(t);
			fireResourceCreationEvent(resource, t);
			if (t instanceof ResourceException) {
				throw (ResourceException) t;
			} else {
				throw new ResourceCreationException(AbstractResourceServiceImpl.RESOURCE_BUNDLE_DEFAULT,
						"generic.create.error", t.getMessage(), t);
			}
		}

		return null;
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate() throws AccessDeniedException {
		assertPermission(DictionaryResourcePermission.READ);
		return repository.getPropertyCategories(null);
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate(Word resource) throws AccessDeniedException {
		assertPermission(DictionaryResourcePermission.READ);
		return repository.getPropertyCategories(resource);
	}

	@Override
	public String randomWord(Locale locale) {
		return dictionaryRepository.randomWord(locale);
	}

	@Override
	public boolean containsWord(Locale locale, String word) throws IOException {
		Boolean doBuiltIn = systemConfigurationService.getBooleanValue("dictionary.blacklistBuiltIn");
		Boolean doApi = systemConfigurationService.getBooleanValue("dictionary.blacklistApiCall");
		Boolean caseInsensitive = systemConfigurationService.getBooleanValue("dictionary.caseInsenstive");
		return (doBuiltIn && dictionaryRepository.containsWord(locale, word, caseInsensitive, true))
				|| (doApi && inRemote(locale, word));
	}

	protected HttpResponse authRemote(Locale locale, String word) throws IOException {
		String method = processTokenReplacements(systemConfigurationService.getValue("dictionary.logonMethod"), locale,
				word, null);
		String url = processTokenReplacements(systemConfigurationService.getValue("dictionary.logonUrl"), locale, word, null);
		String responseContent = processTokenReplacements(
				systemConfigurationService.getValue("dictionary.logonResponseContent"), locale, word, null);
		String[] variables = systemConfigurationService.getValues("dictionary.logonVariables");
		String[] responses = systemConfigurationService.getValues("dictionary.logonResponseList");
		String[] headers = systemConfigurationService.getValues("dictionary.logonHeaders");
		boolean checkCertificate = systemConfigurationService.getBooleanValue("dictionary.certificate");
		String authType = systemConfigurationService.getValue("dictionary.logonAuthentication");
		String username = systemConfigurationService.getValue("dictionary.logonUsername"); 
		String password = systemConfigurationService.getValue("dictionary.logonPassword");

		for (int i = 0; i < responses.length; i++) {
			responses[i] = processTokenReplacements(responses[i], locale, word, null);
		}

		CloseableHttpClient client = null;
		HttpResponse response;
		try {
			client = httpUtils.createHttpClient(!checkCertificate);
			HttpRequestBase request;
			if (METHOD_GET.equals(method)) {
				url = encodeVars(locale, word, url, variables, null);
				request = new HttpGet(url);
			} else {
				request = new HttpPost(url);
				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(variables.length);
				for (String variable : variables) {
					String[] namePair = variable.split("=");
					nameValuePairs.add(new BasicNameValuePair(processTokenReplacements(namePair[0], locale, word, null),
							processTokenReplacements(namePair[1], locale, word, null)));
				}
				((HttpPost)request).setEntity(new UrlEncodedFormEntity(nameValuePairs));
			}
			request.addHeader("User-Agent", USER_AGENT);
			for (String header : headers) {
				request.addHeader(processTokenReplacements(ResourceUtils.getNamePairKey(header), locale, word, null),
						processTokenReplacements(ResourceUtils.getNamePairValue(header), locale, word, null));
			}
			HttpClientContext ctx = auth(client, url, authType, username, password);
			if(ctx == null)
				response = client.execute(request);
			else
				response = client.execute(request, ctx);

			HttpEntity entity = response.getEntity();
			String content = EntityUtils.toString(entity);

			boolean matchesResponse = responses.length == 0 ? true
					: Arrays.asList(responses).contains(Integer.toString(response.getStatusLine().getStatusCode()));
			boolean matchesContent = StringUtils.isBlank(content) || Pattern.compile(responseContent, Pattern.DOTALL).matcher(content).matches();

			return matchesResponse && matchesContent ? response : null;

		} catch (Exception e) {
			throw new IOException("Failed to fully process login " + method + " method for " + url + "variables: " + variables, e);
		} finally {
			if (client != null) {
				try {
					client.close();
				} catch (IOException e) {
					log.error("Error closing login HttpClient instance for HTTP Task", e);
				}
			}
		}
	}

	protected boolean inRemote(Locale locale, String word) throws IOException {

		HttpResponse logonResponse = null;
		if (systemConfigurationService.getBooleanValue("dictionary.logonRequired")) {
			logonResponse = authRemote(locale, word);
			if (logonResponse == null)
				throw new IOException("Authentication with banned passwords server failed.");
		}

		String method = processTokenReplacements(systemConfigurationService.getValue("dictionary.method"), locale,
				word, logonResponse);
		String url = processTokenReplacements(systemConfigurationService.getValue("dictionary.url"), locale, word, logonResponse);
		String responseContent = processTokenReplacements(
				systemConfigurationService.getValue("dictionary.responseContent"), locale, word, logonResponse);
		String[] variables = systemConfigurationService.getValues("dictionary.variables");
		String[] responses = systemConfigurationService.getValues("dictionary.responseList");
		String[] headers = systemConfigurationService.getValues("dictionary.headers");
		boolean checkCertificate = systemConfigurationService.getBooleanValue("dictionary.certificate");
		String authType = systemConfigurationService.getValue("dictionary.authentication");
		String username = systemConfigurationService.getValue("dictionary.username");
		String password = systemConfigurationService.getValue("dictionary.password");

		for (int i = 0; i < responses.length; i++) {
			responses[i] = processTokenReplacements(responses[i], locale, word, logonResponse);
		}

		CloseableHttpClient client = null;
		HttpResponse response;
		try {

			client = httpUtils.createHttpClient(!checkCertificate);
			HttpRequestBase request;
			if (METHOD_GET.equals(method)) {
				url = encodeVars(locale, word, url, variables, logonResponse);
				request = new HttpGet(url);

			} else {
				request = new HttpPost(url);

				List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(variables.length);
				for (String variable : variables) {
					String[] namePair = variable.split("=");
					nameValuePairs.add(new BasicNameValuePair(processTokenReplacements(namePair[0], locale, word, logonResponse),
							processTokenReplacements(namePair[1], locale, word, logonResponse)));
				}

				((HttpPost)request).setEntity(new UrlEncodedFormEntity(nameValuePairs));
			}
			if(logonResponse != null) {
				for (Header h : logonResponse.getHeaders("Set-Cookie")) {
					List<HttpCookie> cookies = HttpCookie.parse(h.getValue());
					for (HttpCookie c : cookies) {
						request.addHeader("Cookie", c.toString());
					}
				}
			}
			request.addHeader("User-Agent", USER_AGENT);
			for (String header : headers) {
				request.addHeader(processTokenReplacements(ResourceUtils.getNamePairKey(header), locale, word, logonResponse),
						processTokenReplacements(ResourceUtils.getNamePairValue(header), locale, word, logonResponse));
			}
			HttpClientContext ctx = auth(client, url, authType, username, password);
			if(ctx == null)
				response = client.execute(request);
			else
				response = client.execute(request, ctx);

			HttpEntity entity = response.getEntity();
			String content = EntityUtils.toString(entity);

			boolean matchesResponse = responses.length == 0 ? true
					: Arrays.asList(responses).contains(Integer.toString(response.getStatusLine().getStatusCode()));
			boolean matchesContent = StringUtils.isBlank(content) || Pattern.compile(responseContent, Pattern.DOTALL).matcher(content).matches();

			return matchesResponse && matchesContent;

		} catch (Exception e) {
			throw new IOException("Failed to fully process login " + method + " method for " + url + "variables: " + variables, e);
		} finally {
			if (client != null) {
				try {
					client.close();
				} catch (IOException e) {
					log.error("Error closing HttpClient instance for HTTP Task", e);
				}
			}
		}
	}

	private HttpClientContext auth(CloseableHttpClient client, String url, String authType, String username,
			String password) {
		
		if(StringUtils.isNotBlank(authType) && !authType.equals("NONE")) {
			try {
				URL u = new URL(url);
				CredentialsProvider credsProvider = new BasicCredentialsProvider();
				credsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(username, password));

				AuthCache authCache = new BasicAuthCache();
				BasicScheme basicAuth = new BasicScheme();
				authCache.put(new HttpHost(u.getHost(), u.getPort() == -1 ? 
						(u.getProtocol().equals("http") ? 80 : 443) : u.getPort(), u.getProtocol()), basicAuth);

				// Add AuthCache to the execution context
				HttpClientContext context = HttpClientContext.create();
				context.setCredentialsProvider(credsProvider);
				context.setAuthCache(authCache);

				return context;
			} catch (MalformedURLException murle) {
				throw new IllegalStateException("Not a valid URL.");
			}
		}
		
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deleteResource(Word resource) throws AccessDeniedException, ResourceException {
		assertPermission(DictionaryResourcePermission.DELETE);
		if(resource.getLocale() == null) {
			resource.setLocale(null);
			repository.saveResource(resource);
		}
		repository.deleteResource(resource);
	}

	@Override
	public Word getResourceById(long id) throws ResourceNotFoundException {
		return repository.getResourceById(id);
	}

	@Override
	public List<?> searchResources(Locale locale, String searchColumn, String searchPattern, int start, int length,
			ColumnSort[] sorting) throws AccessDeniedException {
		assertPermission(DictionaryResourcePermission.READ);
		return repository.search(locale, searchColumn, searchPattern, start, length, sorting);
	}

	@Override
	public Long getResourceCount(Locale locale, String searchColumn, String searchPattern)
			throws AccessDeniedException {
		assertPermission(DictionaryResourcePermission.READ);
		return repository.getResourceCount(locale, searchColumn, searchPattern);
	}

	@Override
	@Transactional(readOnly = false)
	public long importDictionary(Locale locale, Reader input, boolean ignoreDuplicates)
			throws ResourceException, IOException, AccessDeniedException {
		assertPermission(DictionaryResourcePermission.CREATE);
		BufferedReader r = new BufferedReader(input);
		String line;
		long l = 0;
		while ((line = r.readLine()) != null) {
			for (String word : line.split("\\s+")) {
				if (ignoreDuplicates && repository.containsWord(locale, word,
						systemConfigurationService.getBooleanValue("dictionary.caseInsenstive"), false))
					continue;
				createResource(locale, word);
				l++;
				if ((l % 100) == 0)
					repository.flush();

			}
		}
		return l;
	}

	@Override
	@Transactional
	public void deleteResources(List<Long> wordIds) throws ResourceException, AccessDeniedException {
		assertPermission(DictionaryResourcePermission.CREATE);
		repository.deleteResources(wordIds);
	}

	private String encodeVars(Locale locale, String word, String url, String[] variables, HttpResponse response)
			throws UnsupportedEncodingException {
		if (variables.length > 0) {
			url = url + "?";
			boolean first = true;
			for (String variable : variables) {
				if (!first) {
					url += "&";
				}
				first = false;
				String[] namePair = variable.split("=");
				url += URLEncoder.encode(processTokenReplacements(namePair[0], locale, word, response), "UTF-8") + "="
						+ URLEncoder.encode(processTokenReplacements(namePair[1], locale, word, response), "UTF-8");
			}
		}
		return url;
	}

	private String processTokenReplacements(String value, Locale locale, String word, HttpResponse response) {
		try {
			if (value == null)
				return null;
			value = value.replace("${word}", word);
			value = value.replace("${encodedWord}", URLEncoder.encode(word, "UTF-8"));
			value = value.replace("${locale}", locale == null ? "" : locale.toLanguageTag());
			if (response != null) {
				for (Header h : response.getHeaders("Set-Cookie")) {
					List<HttpCookie> cookies = HttpCookie.parse(h.getValue());
					for (HttpCookie c : cookies) {
						value = value.replace("${cookie." + c.getName() + "}", c.getValue());
					}
				}
			}
			return value;

		} catch (UnsupportedEncodingException e) {
			return value;
		}
	}

	@Override
	public void onUpgradeFinished() {
	}

	@Override
	public void onUpgradeComplete() {

		new Thread() {
			public void run() {
				try {
					transactionService.doInTransaction(new TransactionCallback<Void>() {

						@Override
						public Void doInTransaction(TransactionStatus arg0) {
							dictionaryRepository.setup();
							return null;
						}

					});
				} catch (ResourceException e) {
					log.error("Could not setup dictionary", e);
				} catch (AccessDeniedException e) {
					log.error("Could not setup dictionary", e);
				}

			}
		}.start();
	}
}
