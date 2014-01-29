package com.hypersocket.notify;

import java.util.List;

import com.hypersocket.auth.AuthenticatedService;

public interface NotificationService extends AuthenticatedService {

	public final static String RESOURCE_BUNDLE = "NotificationService";
	
	List<Notification> getNotifications(String context);

	void registerProvider(NotificationProvider provider);

	void unregisterProvider(NotificationProvider provider);

}
