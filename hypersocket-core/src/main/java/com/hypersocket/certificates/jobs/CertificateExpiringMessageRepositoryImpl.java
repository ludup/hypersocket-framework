package com.hypersocket.certificates.jobs;

import javax.annotation.PostConstruct;

import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hypersocket.automation.AutomationRepeatType;
import com.hypersocket.automation.SchedulingResourceService;
import com.hypersocket.message.MessageResource;
import com.hypersocket.properties.ResourceTemplateRepositoryImpl;

@Repository
public class CertificateExpiringMessageRepositoryImpl extends ResourceTemplateRepositoryImpl implements CertificateExpiringMessageRepository {

	@Autowired
	private SchedulingResourceService resourceScheduler;
	
	@PostConstruct
	private void postConstruct() {
		loadPropertyTemplates("certificateExpiring.xml", getClass().getClassLoader());
	}

	@Override
	public void onCreated(MessageResource resource) {
		
		String time = getValue(resource, "certificate.reminder.time");
		scheduleExpiring(resource, time);
		
	}

	protected void scheduleExpiring(MessageResource resource, String time) {
		resourceScheduler.schedule(resource, null, time, null, null, AutomationRepeatType.DAYS, 1, CertificateExpiringNotificationJob.class);
	}
	
	@Override
	public void onUpdated(MessageResource resource) {
		try {
			resourceScheduler.unschedule(resource);
		} catch (SchedulerException e) {
		}
		String time = getValue(resource, "certificate.reminder.time");
		scheduleExpiring(resource, time);
	}

	@Override
	public void onDeleted(MessageResource resource) {
		try {
			resourceScheduler.unschedule(resource);
		} catch (SchedulerException e) {
		}
	}

}
