package com.hypersocket.dashboard.message.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.attributes.Attribute;
import com.hypersocket.dashboard.message.DashboardMessage;
import com.hypersocket.session.Session;
import com.hypersocket.session.events.SessionEvent;

public class DashboardMessageEvent extends SessionEvent {

	private static final long serialVersionUID = -1438866177845416396L;
	
	public static final String EVENT_RESOURCE_KEY = "dashboardMessage.event";
	public static final String ATTR_DASHBOARD_MESSAGE_NAME = "attr.dashboardMessageName";

	public DashboardMessageEvent(Object source, String resourceKey, boolean success,
			Session session, DashboardMessage message) {
		super(source, resourceKey, success, session);

		addAttribute(ATTR_DASHBOARD_MESSAGE_NAME, message.getName());
	}

	public DashboardMessageEvent(Object source, String resourceKey, Throwable e,
			Session session, DashboardMessage message) {
		super(source, resourceKey, e, session);
		addAttribute(ATTR_DASHBOARD_MESSAGE_NAME, message.getName());
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
