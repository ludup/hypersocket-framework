package com.hypersocket.email;

import com.hypersocket.batch.BatchProcessingService;
import com.hypersocket.realm.Realm;

public interface EmailBatchService extends BatchProcessingService<EmailBatchItem> {

	void queueEmail(Realm realm, String subject, String plainText, String html, String replyToName, String replyToEmail,
			RecipientHolder[] recipients, String[] archiveAddresses, Boolean track, Boolean useTemplate);

}
