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

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.HypersocketVersion;
import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.dashboard.message.DashboardMessageService;
import com.hypersocket.http.HttpUtilsImpl;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.scheduler.SchedulerService;

@Service
public class OverviewWidgetServiceImpl extends AbstractAuthenticatedServiceImpl
		implements OverviewWidgetService {

	public static final String RESOURCE_BUNDLE = "OverviewWidgetService";
	private Map<String,List<OverviewWidget>> widgetList = new HashMap<String,List<OverviewWidget>>();

	public static final String HELPZONE = "helpzone";
	public static final String USERDASH = "userdash";
	
	static final String MENU_DASHBOARD_HELPZONE = "helpzone"; 
	
	private Collection<Link> cachedLinks = null;
	private Collection<Link> cachedVideos = null;
	private Collection<Link> cachedDocumentation = null;

	private long lastLinkUpdate = 0;
	private long lastVideoUpdate = 0;
	private long lastDocumentationUpdate = 0;

	@Autowired
	I18NService i18nService;

	@Autowired
	SchedulerService schedulerService;

	@Autowired
	PermissionService permissionService;

	@Autowired
	DashboardMessageService messageService; 
	
	@Autowired
	HttpUtilsImpl httpUtils;
	
	@PostConstruct
	private void postConstruct() {
		i18nService.registerBundle(RESOURCE_BUNDLE);

	
//		menuService.registerMenu(new MenuRegistration(RESOURCE_BUNDLE, MENU_DASHBOARD_HELPZONE,
//				"fa-graduation-cap", "helpzone", 0, SystemPermission.SYSTEM_ADMINISTRATION, null,
//				null, null), MenuService.MENU_DASHBOARD);
		
		this.registerWidget(HELPZONE, new OverviewWidget(4, "overview.usefulLinks.title",
				"usefulLinks", false) {
			public boolean hasContent() {
				try {
					return getLinks().size() > 0;
				} catch (ResourceException e) {
					return false;
				}
			}
		});
		
		this.registerWidget(HELPZONE, new OverviewWidget(1,
				"overview.systemMessages.title", "systemMessages", true) {
			public boolean hasContent() {
				return messageService.getMessageCount() > 0;
			}
		});
		
		this.registerWidget(HELPZONE, new OverviewWidget(3, "overview.featureReel.title",
				"featureReel", true) {
			public boolean hasContent() {
				try {
					return getVideos().size() > 0;
				} catch (ResourceException e) {
					return false;
				}
			}
		});
		
//		this.registerWidget(HELPZONE, new OverviewWidget(-1, "overview.gettingStarted.title",
//				System.getProperty("hypersocket.id", "hypersocket-vpn"), true) {
//			public boolean hasContent() {
//				return true;
//			}
//		});
		
		this.registerWidget(HELPZONE, new OverviewWidget(3, "overview.quickSetup.title",
				"quickSetup", false) {
			public boolean hasContent() {
				try {
					return getDocumentation().size() > 0;
				} catch (ResourceException e) {
					return false;
				}
			}
		});
		
		
	}

	public void registerWidget(String resourceKey, OverviewWidget widget) {
		if(!widgetList.containsKey(resourceKey)) {
			widgetList.put(resourceKey, new ArrayList<OverviewWidget>());
		}
		widgetList.get(resourceKey).add(widget);
		Collections.sort(widgetList.get(resourceKey));
	}

	public List<OverviewWidget> getWidgets(String resourceKey) throws AccessDeniedException {

		List<OverviewWidget> visibleWidgets = new ArrayList<OverviewWidget>();
		if(widgetList!=null) {
			if(widgetList.containsKey(resourceKey)) {
				for (OverviewWidget w : widgetList.get(resourceKey)) {
					if (w.hasContent()) {
						visibleWidgets.add(w);
					}
				}
			}
		}
		return visibleWidgets;
	}

	@Override
	public Collection<Link> getLinks() throws ResourceException {

		if (System.currentTimeMillis() - lastLinkUpdate > (24 * 60 * 60 * 1000)) {

			ObjectMapper mapper = new ObjectMapper();

			try {

				List<Link> results = new ArrayList<Link>();

				String content = IOUtils.toString(httpUtils
						.doHttpGet("http://updates.hypersocket.com/messages/"
								+ HypersocketVersion.getBrandId()
								+ "/links.json", true));
				
				// Get global links
				results.addAll(Arrays.asList(mapper.readValue(content, Link[].class)));

				// Get product links
				results.addAll(Arrays.asList(mapper.readValue(httpUtils
						.doHttpGet("http://updates.hypersocket.com/messages/"
								+ HypersocketVersion.getProductId()
								+ "/links.json", true), Link[].class)));

				cachedLinks = sort(results);
				lastLinkUpdate = System.currentTimeMillis();
			} catch (Throwable e) {
				e.printStackTrace();
				throw new ResourceException(RESOURCE_BUNDLE,
						"error.readingArticleList", e.getMessage());
			}
		}

		return cachedLinks;
	}

	@Override
	public Collection<Link> getVideos() throws ResourceException {

		if (System.currentTimeMillis() - lastVideoUpdate > (24 * 60 * 60 * 1000)) {

			ObjectMapper mapper = new ObjectMapper();

			try {

				List<Link> results = new ArrayList<Link>();

				// Get global videos
				results.addAll(Arrays.asList(mapper.readValue(httpUtils
						.doHttpGet("http://updates.hypersocket.com/messages/"
								+ HypersocketVersion.getBrandId()
								+ "/videos.json", true), Link[].class)));

				// Get product videos
				results.addAll(Arrays.asList(mapper.readValue(httpUtils
						.doHttpGet("http://updates.hypersocket.com/messages/"
								+ HypersocketVersion.getProductId()
								+ "/videos.json", true), Link[].class)));

				cachedVideos = sort(results);
				lastVideoUpdate = System.currentTimeMillis();
			} catch (Throwable e) {
				throw new ResourceException(RESOURCE_BUNDLE,
						"error.readingArticleList", e.getMessage());
			}
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

			ObjectMapper mapper = new ObjectMapper();

			try {

				List<Link> results = new ArrayList<Link>();

				// Get global documentation
				results.addAll(Arrays.asList(mapper.readValue(httpUtils
						.doHttpGet("http://updates.hypersocket.com/messages/"
								+ HypersocketVersion.getBrandId()
								+ "/documentation.json", true), Link[].class)));

				// Get product documentation
				results.addAll(Arrays.asList(mapper.readValue(httpUtils
						.doHttpGet("http://updates.hypersocket.com/messages/"
								+ HypersocketVersion.getProductId()
								+ "/documentation.json", true), Link[].class)));

				cachedDocumentation = sort(results);
				lastDocumentationUpdate = System.currentTimeMillis();
			} catch (Throwable e) {
				throw new ResourceException(RESOURCE_BUNDLE,
						"error.readingDocumentationList", e.getMessage());
			}
		}

		return cachedDocumentation;
	}
}
