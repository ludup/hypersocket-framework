package com.hypersocket.dashboard.message.events;

import org.apache.commons.lang3.ArrayUtils;

import com.hypersocket.dashboard.message.DashboardMessage;
import com.hypersocket.session.Session;

public class DashboardMessageCreatedEvent extends DashboardMessageEvent {

	private static final long serialVersionUID = -7174259161714227120L;

	public static final String EVENT_RESOURCE_KEY = "event.dashboardMessageCreated";

	public DashboardMessageCreatedEvent(Object source, Session session,
			DashboardMessage message) {
		super(source, EVENT_RESOURCE_KEY, true, session, message);
	}

	public DashboardMessageCreatedEvent(Object source, Throwable e, Session session,
			DashboardMessage message) {
		super(source, EVENT_RESOURCE_KEY, e, session, message);
	}

	public String[] getResourceKeys() {
		return ArrayUtils.add(super.getResourceKeys(), EVENT_RESOURCE_KEY);
	}
}
