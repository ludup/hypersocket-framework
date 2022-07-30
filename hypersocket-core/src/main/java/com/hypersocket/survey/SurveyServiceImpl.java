package com.hypersocket.survey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextStartedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.RealmService;
import com.hypersocket.scheduler.ClusteredSchedulerService;
import com.hypersocket.utils.HttpUtils;

@Service
public class SurveyServiceImpl implements SurveyService {
	public static final String USER_AGENT = "Mozilla/5.0";

	static Logger LOG = LoggerFactory.getLogger(SurveyServiceImpl.class);

	private static final String SURVEYS_READY = "surveys.ready";

	@Autowired
	private HttpUtils httpUtils;
	@Autowired
	private RealmService realmService;

	@Autowired
	private ClusteredSchedulerService schedulerService;

	@Autowired
	private PermissionService permissionService;

	private boolean configurationRetrieved;
	private LicenseStateProvider licenseStateProvider = new LicenseStateProvider() {
		@Override
		public String getState() {
			return "opensource";
		}

		@Override
		public String getSubmissionUrl() {
			return "http://localhost/survey.cgi";
		}

		@Override
		public String getConfigurationUrl() {
			return "http://localhost/survey-configuration.json";
		}
	};
	private Set<String> ready = Collections.synchronizedSet(new LinkedHashSet<>());
	private Set<String> registeredSurveyNames = new HashSet<>();
	private ScheduledExecutorService scheduler;
	private Map<String, Survey> surveys = Collections.synchronizedMap(new HashMap<>());

	public SurveyServiceImpl() {
	}

	@Override
	@EventListener
	public void contextStarted(ContextStartedEvent started) {
		realmService.runAsSystemContext(() -> {
			ready.addAll(Arrays.asList(ResourceUtils
					.explodeValues(realmService.getRealmProperty(realmService.getSystemRealm(), SURVEYS_READY))));
			getSurveyConfiguration();
			scheduler = Executors.newSingleThreadScheduledExecutor();
			scheduler.scheduleAtFixedRate(() -> {
				realmService.runAsSystemContext(() -> getSurveyConfiguration());
			}, 1, 1, TimeUnit.DAYS);
		});

	}

	@Override
	public Survey getNextReady() throws AccessDeniedException {
		permissionService.assertAdministrativeAccess();
		synchronized (ready) {
			if (ready.isEmpty())
				throw new IllegalStateException();
			return getSurvey(ready.iterator().next());
		}
	}

	@Override
	public Survey getSurvey(String survey) {
		if (registeredSurveyNames.contains(survey))
			return surveys.get(survey);
		return null;
	}

	@Override
	public boolean isSurveyReady(String survey) {
		return ready.contains(survey);
	}

	@Override
	public Survey registerSurvey(String resourceKey) {
		Survey survey = surveys.get(resourceKey);
		if (registeredSurveyNames.contains(resourceKey))
			throw new IllegalStateException(String.format("Survey %s already registered.", resourceKey));
		else {
			registeredSurveyNames.add(resourceKey);
			if (survey == null) {
				LOG.debug(
						"The survey {} does not currently exist. It may appear later, at which point its triggers will be activated.",
						resourceKey);
			}
			schedule();
		}
		return survey;
	}

	@Override
	public void schedule() {
		synchronized (surveys) {
			if (!configurationRetrieved) {
				return;
			}

			String state = licenseStateProvider.getState();
			LOG.info("Checking for jobs to schedule for current license state of {}", state);

			/**
			 * Iterate through all surveys, and schedule jobs for those that ...
			 * 
			 * 1. Have been registered (we only want surveys applicable to this product) 2.
			 * Are not already "ready", i.e. will be displayed to the target user when they
			 * are available 3. Have triggers for the current license state 4. Have never
			 * been run before, or have been run before but have a different serial.
			 */
			Set<Survey> invalidSurveys = new HashSet<>(surveys.values());
			for (Survey en : surveys.values()) {
				if (!ready.contains(en.getResourceKey()) && registeredSurveyNames.contains(en.getResourceKey())) {
					int serial = realmService.getRealmPropertyInt(realmService.getSystemRealm(),
							en.getResourceKey() + ".serial");
					if (serial < en.getSerial() && !en.getTriggers(state).isEmpty()) {
						if (!en.isScheduled()) {
							LOG.info("Need to schedule survey {}", en.getResourceKey());
							en.schedule(state);
						}
						invalidSurveys.remove(en);
					}
				}
			}

			/**
			 * Close any surveys that are no longer valid (and have valid triggers)
			 */
			for (Survey s : invalidSurveys) {
				if (s.isScheduled()) {
					LOG.info("Survey {} is no longer valid, unscheduling", s.getResourceKey());
					try {
						s.close();
					} catch (IOException ioe) {
						LOG.error("Failed to close scheduled survey " + s.getResourceKey(), ioe);
					}
				}
			}
		}
	}

	public void setLicenseStateProvider(LicenseStateProvider licenseStateProvider) {
		this.licenseStateProvider = licenseStateProvider;
	}

	@Override
	public void submitSurvey(String resourceKey, Map<String, String[]> data) throws IOException, AccessDeniedException {
		permissionService.assertAdministrativeAccess();
		Survey survey = getSurvey(resourceKey);
		if (survey == null)
			throw new IOException("No survey " + resourceKey);
		try (CloseableHttpClient client = httpUtils.createHttpClient(true)) {
			HttpPost request = new HttpPost(licenseStateProvider.getSubmissionUrl());
			request.addHeader("User-Agent", USER_AGENT);
			request.addHeader("Content-Type", "application/x-www-form-urlencoded");

			List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();
			nameValuePairs.add(new BasicNameValuePair("resourceKey", resourceKey));
			for (Map.Entry<String, String[]> en : data.entrySet()) {
				if (!en.getKey().equals("resourceKey")) {
					for (String v : en.getValue()) {
						nameValuePairs.add(new BasicNameValuePair(en.getKey(), v));
					}
				}
			}
			request.setEntity(new UrlEncodedFormEntity(nameValuePairs));
			CloseableHttpResponse response = client.execute(request);
			if (200 != response.getStatusLine().getStatusCode()) {
				throw new IOException(
						String.format("Unexpected response code %d.", response.getStatusLine().getStatusCode()));
			}
			LOG.info(String.format("Submitted %s survey.", resourceKey));
			try {
				realmService.setRealmProperty(realmService.getSystemRealm(), resourceKey + ".serial",
						String.valueOf(survey.getSerial()));
			} catch (AccessDeniedException e) {
				throw new IOException("Failed to update serial number for survey " + resourceKey
						+ ". Note, the survey was submitted.");
			}
			ready.remove(survey.getResourceKey());
			saveReady();
		} finally {
			schedule();
		}
	}

	@Override
	public void surveyReady(String survey) {
		synchronized (ready) {
			Survey s = getSurvey(survey);
			if (s == null)
				throw new IllegalArgumentException("No such survey as " + survey);
			LOG.info("Survey {} now ready.", survey);
			ready.add(survey);
			saveReady();
			schedule();
		}
	}

	protected void getSurveyConfiguration() {
		synchronized (surveys) {
			LOG.info("Retrieving survey configuration.");
			try (CloseableHttpClient client = httpUtils.createHttpClient(true)) {
				HttpGet request = new HttpGet(licenseStateProvider.getConfigurationUrl());

				request.addHeader("User-Agent", USER_AGENT);
				request.addHeader("Content-Type", "application/json");

				CloseableHttpResponse response = client.execute(request);

				HttpEntity entity = response.getEntity();
				String responseContent = EntityUtils.toString(entity);

				if (200 == response.getStatusLine().getStatusCode()) {

					JsonParser p = new JsonParser();
					JsonObject root = p.parse(responseContent).getAsJsonObject();

					/* Add or update any that still exist */
					for (Entry<String, JsonElement> surveyElEn : root.entrySet()) {
						Survey survey = new Survey(surveyElEn.getKey(), surveyElEn.getValue().getAsJsonObject(),
								realmService, schedulerService);
						Survey currentSurvey = surveys.get(surveyElEn.getKey());
						if (!Objects.equals(survey, currentSurvey)) {
							if (currentSurvey != null)
								currentSurvey.close();

							surveys.put(surveyElEn.getKey(), survey);
						}
					}

					/* Remove any that don't exist any more */
					for (Map.Entry<String, Survey> en : new HashMap<>(surveys).entrySet()) {
						if (!root.has(en.getKey())) {
							LOG.info("Removing survey {}, it is no longer valid.", en.getKey());
							surveys.remove(en.getKey());
							if (en.getValue().isScheduled())
								en.getValue().close();
						}
					}

					configurationRetrieved = true;
					schedule();
				} else {
					throw new IOException(
							String.format("Unexpected response code %d.", response.getStatusLine().getStatusCode()));
				}

			} catch (Exception e) {
				LOG.warn("Failed to get survey configuration. Assuming defaults. ", e);
			}
		}
	}

	protected void saveReady() {
		try {
			realmService.setRealmProperty(realmService.getSystemRealm(), SURVEYS_READY,
					ResourceUtils.implodeValues(ready));
		} catch (AccessDeniedException e) {
			throw new IllegalStateException("Failed to update ready realms.");
		}
	}

	@PostConstruct
	private void setup() {
	}

	@Override
	public Survey reject(String resourceKey) throws AccessDeniedException {
		synchronized (ready) {
			permissionService.assertAdministrativeAccess();
			Survey survey = getSurvey(resourceKey);
			LOG.info("Rejecting survey {} for now.", resourceKey);
			ready.remove(resourceKey);
			saveReady();
			schedule();
			return survey;
		}
	}
}
