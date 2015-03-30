package com.hypersocket.notify;

import java.util.ArrayList;
import java.util.List;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;

public class NotificationServiceImpl extends AbstractAuthenticatedServiceImpl implements NotificationService {

	List<NotificationProvider> providers = new ArrayList<NotificationProvider>();
	
	@Override
	public void registerProvider(NotificationProvider provider) {
		providers.add(provider);
	}
	
	@Override
	public void unregisterProvider(NotificationProvider provider) {
		providers.remove(provider);
	}
	
	@Override
	public List<Notification> getNotifications(String context) {
	
		List<Notification> results = new ArrayList<Notification>();
		for(NotificationProvider p : providers) {
			if(p.hasNotifications(getCurrentSession(), context)) {
				results.addAll(p.getNotifications(getCurrentSession(), getCurrentLocale(), context));
			}
		}
		return results;
	}
}
