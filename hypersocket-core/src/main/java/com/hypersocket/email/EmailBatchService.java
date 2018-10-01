package com.hypersocket.email;

import java.util.Date;

import com.hypersocket.batch.BatchProcessingService;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceException;

public interface EmailBatchService extends BatchProcessingService<EmailBatchItem> {

	void queueEmail(Realm realm, String subject, String body, String html, String replyToName,
			String replyToEmail, String name, String email, Boolean track, String attachments) throws ResourceException;
	
	void scheduleEmail(Realm realm, String subject, String body, String html, String replyToName,
			String replyToEmail, String name, String email, Boolean track, String attachments, Date schedule) throws ResourceException;

}
