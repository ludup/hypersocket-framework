package com.hypersocket.dashboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.HypersocketVersion;
import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.dashboard.message.DashboardMessageService;
import com.hypersocket.http.HttpUtils;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.scheduler.SchedulerService;

@Service
public class OverviewWidgetServiceImpl extends AbstractAuthenticatedServiceImpl
		implements OverviewWidgetService {

	public static final String RESOURCE_BUNDLE = "OverviewWidgetService";
	private List<OverviewWidget> widgetList = new ArrayList<OverviewWidget>();

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
	
	@PostConstruct
	private void postConstruct() {
		i18nService.registerBundle(RESOURCE_BUNDLE);

		this.registerWidget(new OverviewWidget(2, "overview.yourSerial.title",
				"yourSerial", false) {
			public boolean hasContent() {
				return true;
			}
		});
		
		this.registerWidget(new OverviewWidget(4, "overview.usefulLinks.title",
				"usefulLinks", false) {
			public boolean hasContent() {
				try {
					return getLinks().size() > 0;
				} catch (ResourceException e) {
					return false;
				}
			}
		});
		
		this.registerWidget(new OverviewWidget(1,
				"overview.systemMessages.title", "systemMessages", true) {
			public boolean hasContent() {
				return messageService.getMessageCount() > 0;
			}
		});
		
		this.registerWidget(new OverviewWidget(3, "overview.featureReel.title",
				"featureReel", true) {
			public boolean hasContent() {
				try {
					return getVideos().size() > 0;
				} catch (ResourceException e) {
					return false;
				}
			}
		});
		
		this.registerWidget(new OverviewWidget(3, "overview.quickSetup.title",
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

	public void registerWidget(OverviewWidget widget) {
		widgetList.add(widget);
		Collections.sort(widgetList);
	}

	public List<OverviewWidget> getWidgets() throws AccessDeniedException {

		List<OverviewWidget> visibleWidgets = new ArrayList<OverviewWidget>();
		for (OverviewWidget w : widgetList) {
			if (w.hasContent()) {
				visibleWidgets.add(w);
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

				String content = IOUtils.toString(HttpUtils
						.doHttpGet("http://updates.hypersocket.com/messages/"
								+ HypersocketVersion.getBrandId()
								+ "/links.json", true));
				
				// Get global links
				results.addAll(Arrays.asList(mapper.readValue(content, Link[].class)));

				// Get product links
				results.addAll(Arrays.asList(mapper.readValue(HttpUtils
						.doHttpGet("http://updates.hypersocket.com/messages/"
								+ HypersocketVersion.getProductId()
								+ "/links.json", true), Link[].class)));

				cachedLinks = results;
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
				results.addAll(Arrays.asList(mapper.readValue(HttpUtils
						.doHttpGet("http://updates.hypersocket.com/messages/"
								+ HypersocketVersion.getBrandId()
								+ "/videos.json", true), Link[].class)));

				// Get product videos
				results.addAll(Arrays.asList(mapper.readValue(HttpUtils
						.doHttpGet("http://updates.hypersocket.com/messages/"
								+ HypersocketVersion.getProductId()
								+ "/videos.json", true), Link[].class)));

				cachedVideos = results;
				lastVideoUpdate = System.currentTimeMillis();
			} catch (Throwable e) {
				throw new ResourceException(RESOURCE_BUNDLE,
						"error.readingArticleList", e.getMessage());
			}
		}

		return cachedVideos;
	}

	@Override
	public Collection<Link> getDocumentation() throws ResourceException {

		if (System.currentTimeMillis() - lastDocumentationUpdate > (24 * 60 * 60 * 1000)) {

			ObjectMapper mapper = new ObjectMapper();

			try {

				List<Link> results = new ArrayList<Link>();

				// Get global documentation
				results.addAll(Arrays.asList(mapper.readValue(HttpUtils
						.doHttpGet("http://updates.hypersocket.com/messages/"
								+ HypersocketVersion.getBrandId()
								+ "/documentation.json", true), Link[].class)));

				// Get product documentation
				results.addAll(Arrays.asList(mapper.readValue(HttpUtils
						.doHttpGet("http://updates.hypersocket.com/messages/"
								+ HypersocketVersion.getProductId()
								+ "/documentation.json", true), Link[].class)));

				cachedDocumentation = results;
				lastDocumentationUpdate = System.currentTimeMillis();
			} catch (Throwable e) {
				throw new ResourceException(RESOURCE_BUNDLE,
						"error.readingDocumentationList", e.getMessage());
			}
		}

		return cachedDocumentation;
	}
}
