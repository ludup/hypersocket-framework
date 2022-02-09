package com.hypersocket.dashboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.http.HttpUtilsImpl;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.json.version.HypersocketVersion;
import com.hypersocket.resource.ResourceException;

@Service
public class OverviewWidgetServiceImpl extends AbstractAuthenticatedServiceImpl
		implements OverviewWidgetService {

	public static final String RESOURCE_BUNDLE = "OverviewWidgetService";
	private Map<String,List<OverviewWidget>> widgetList = new HashMap<>();

	static Logger log = LoggerFactory.getLogger(OverviewWidgetServiceImpl.class);

	public static final String HELPZONE = "helpzone";
	public static final String USERDASH = "userdash";

	private Collection<Link> cachedLinks = null;
	private Collection<Link> cachedVideos = null;
	private Collection<Link> cachedDocumentation = null;
	private Collection<Link> cachedFirstSteps = null;

	private long lastLinkUpdate = 0;
	private long lastVideoUpdate = 0;
	private long lastDocumentationUpdate = 0;
	private long lastFirstStepsUpdate = 0;

	@Autowired
	private I18NService i18nService;

	@Autowired
	private HttpUtilsImpl httpUtils;

	@PostConstruct
	private void postConstruct() {
		i18nService.registerBundle(RESOURCE_BUNDLE);


//		this.registerWidget(HELPZONE, new OverviewWidget(4, "overview.usefulLinks.title",
//				"usefulLinks", false) {
//			public boolean hasContent() {
//				try {
//					return getLinks().size() > 0;
//				} catch (ResourceException e) {
//					return false;
//				}
//			}
//		});
//
//		this.registerWidget(HELPZONE, new OverviewWidget(3, "overview.featureReel.title",
//				"featureReel", true) {
//			public boolean hasContent() {
//				try {
//					return getVideos().size() > 0;
//				} catch (ResourceException e) {
//					return false;
//				}
//			}
//		});
//
//		this.registerWidget(HELPZONE, new OverviewWidget(3, "overview.quickSetup.title",
//				"quickSetup", false) {
//			public boolean hasContent() {
//				try {
//					return getDocumentation().size() > 0;
//				} catch (ResourceException e) {
//					return false;
//				}
//			}
//		});
//
//		this.registerWidget(HELPZONE, new OverviewWidget(0, "overview.firstSteps.title",
//				"firstSteps", true) {
//			public boolean hasContent() {
//				try {
//					return getFirstSteps().size() > 0;
//				} catch (ResourceException e) {
//					return false;
//				}
//			}
//		});

//		Thread t = new Thread() {
//			public void run() {
//				try {
//					getDocumentation();
//				} catch (ResourceException e) {
//				}
//				try {
//					getVideos();
//				} catch (ResourceException e) {
//				}
//				try {
//					getFirstSteps();
//				} catch (ResourceException e) {
//				}
//				try {
//					getLinks();
//				} catch (ResourceException e) {
//				}
//			}
//		};
//		t.start();
	}

	@Override
	public void registerWidget(String resourceKey, OverviewWidget widget) {
		if(!widgetList.containsKey(resourceKey)) {
			widgetList.put(resourceKey, new ArrayList<OverviewWidget>());
		}
		widgetList.get(resourceKey).add(widget);
		Collections.sort(widgetList.get(resourceKey));
	}

	@Override
	public List<OverviewWidget> getWidgets(String resourceKey) {

		List<OverviewWidget> visibleWidgets = new ArrayList<>();
		if(widgetList!=null) {
			if(widgetList.containsKey(resourceKey)) {
				for (OverviewWidget w : widgetList.get(resourceKey)) {
					if((w.isSystem() && getCurrentRealm().isSystem()) || !w.isSystem()){
						if(w.hasContent()) {
							visibleWidgets.add(w);
						}
					}
				}
			}
		}
		Collections.sort(visibleWidgets);
		return visibleWidgets;
	}

	@Override
	public List<OverviewWidget> getAllWidgets(String resourceKey) {

		List<OverviewWidget> visibleWidgets = new ArrayList<>();
		if(widgetList!=null) {
			if(widgetList.containsKey(resourceKey)) {
				for (OverviewWidget w : widgetList.get(resourceKey)) {
					if((w.isSystem() && getCurrentRealm().isSystem()) || !w.isSystem()){
						visibleWidgets.add(w);
					}
				}
			}
		}
		Collections.sort(visibleWidgets);
		return visibleWidgets;
	}

	@Override
	public Collection<Link> getLinks() throws ResourceException {

		if (System.currentTimeMillis() - lastLinkUpdate > (24 * 60 * 60 * 1000)) {

			log.info("getLinks in");

			ObjectMapper mapper = new ObjectMapper();

			List<Link> results = new ArrayList<>();

			try {

				// Get global documentation
				results.addAll(Arrays.asList(mapper.readValue(httpUtils
						.doHttpGet("https://updates2.hypersocket.com/hypersocket/api/webhooks/publish/links-"
								+ HypersocketVersion.getBrandId(), true), Link[].class)));

				// Get product documentation
				results.addAll(Arrays.asList(mapper.readValue(httpUtils
						.doHttpGet("https://updates2.hypersocket.com/hypersocket/api/webhooks/publish/links-"
								+ HypersocketVersion.getProductId(), true), Link[].class)));

			} catch (Throwable e) {
				throw new ResourceException(RESOURCE_BUNDLE,
						"error.readingArticleList", e.getMessage());
			} finally {
				cachedLinks = sort(results);
				lastLinkUpdate = System.currentTimeMillis();
			}

			log.info("getLinks out");
		}

		return cachedLinks;
	}

	@Override
	public Collection<Link> getVideos() throws ResourceException {

		if (System.currentTimeMillis() - lastVideoUpdate > (24 * 60 * 60 * 1000)) {

			log.info("getVideos in");

			ObjectMapper mapper = new ObjectMapper();

			List<Link> results = new ArrayList<>();

			try {

				// Get global documentation
				results.addAll(Arrays.asList(mapper.readValue(httpUtils
						.doHttpGet("https://updates2.hypersocket.com/hypersocket/api/webhooks/publish/videos-"
								+ HypersocketVersion.getBrandId(), true), Link[].class)));

				// Get product documentation
				results.addAll(Arrays.asList(mapper.readValue(httpUtils
						.doHttpGet("https://updates2.hypersocket.com/hypersocket/api/webhooks/publish/videos-"
								+ HypersocketVersion.getProductId(), true), Link[].class)));


			} catch (Throwable e) {
				throw new ResourceException(RESOURCE_BUNDLE,
						"error.readingArticleList", e.getMessage());
			} finally {
				cachedVideos = sort(results);
				lastVideoUpdate = System.currentTimeMillis();
			}

			log.info("getVideos out");
		}

		return cachedVideos;
	}

	private Collection<Link> sort(List<Link> toSort) {
		Collections.sort(toSort, new Comparator<Link>() {

			@Override
			public int compare(Link o1, Link o2) {
				return o1.getWeight().compareTo(o2.getWeight());
			}
		});

		return toSort;
	}

	@Override
	public Collection<Link> getDocumentation() throws ResourceException {

		if (System.currentTimeMillis() - lastDocumentationUpdate > (24 * 60 * 60 * 1000)) {

			log.info("getDocumentation in");
			ObjectMapper mapper = new ObjectMapper();

			List<Link> results = new ArrayList<>();

			try {

				// Get global documentation
				results.addAll(Arrays.asList(mapper.readValue(httpUtils
						.doHttpGet("https://updates2.hypersocket.com/hypersocket/api/webhooks/publish/docs-"
								+ HypersocketVersion.getBrandId(), true), Link[].class)));

				// Get product documentation
				results.addAll(Arrays.asList(mapper.readValue(httpUtils
						.doHttpGet("https://updates2.hypersocket.com/hypersocket/api/webhooks/publish/docs-"
								+ HypersocketVersion.getProductId(), true), Link[].class)));


			} catch (Throwable e) {
				throw new ResourceException(RESOURCE_BUNDLE,
						"error.readingDocumentationList", e.getMessage());
			} finally {
				cachedDocumentation = sort(results);
				lastDocumentationUpdate = System.currentTimeMillis();
			}

			log.info("getDocumentation ouot");
		}

		return cachedDocumentation;
	}

	@Override
	public Collection<Link> getFirstSteps() throws ResourceException {

		if (System.currentTimeMillis() - lastFirstStepsUpdate > (24 * 60 * 60 * 1000)) {

			log.info("getFirstSteps in");

			ObjectMapper mapper = new ObjectMapper();

			List<Link> results = new ArrayList<>();

			try {


				// Get global documentation
				results.addAll(Arrays.asList(mapper.readValue(httpUtils
						.doHttpGet("https://updates2.hypersocket.com/hypersocket/api/webhooks/publish/steps-"
								+ HypersocketVersion.getBrandId(), true), Link[].class)));

				// Get product documentation
				results.addAll(Arrays.asList(mapper.readValue(httpUtils
						.doHttpGet("https://updates2.hypersocket.com/hypersocket/api/webhooks/publish/steps-"
								+ HypersocketVersion.getProductId(), true), Link[].class)));

			} catch (Throwable e) {
				throw new ResourceException(RESOURCE_BUNDLE,
						"error.readingDocumentationList", e.getMessage());
			} finally {
				cachedFirstSteps = sort(results);
				lastFirstStepsUpdate = System.currentTimeMillis();
			}

			log.info("getFirstSteps out");
		}

		return cachedFirstSteps;
	}

	@Override
	public boolean hasActiveWidgets(String resourceKey) {
		return false; //!getWidgets(resourceKey).isEmpty();
	}
}
