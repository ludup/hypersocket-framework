package com.hypersocket.email;

import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.batch.BatchProcessingItemRepository;
import com.hypersocket.batch.BatchProcessingServiceImpl;
import com.hypersocket.realm.Realm;

public class EmailBatchServiceImpl extends BatchProcessingServiceImpl<EmailBatchItem> implements EmailBatchService {

	@Autowired
	EmailBatchRepository repository;
	
	@Override
	protected BatchProcessingItemRepository<EmailBatchItem> getRepository() {
		return repository;
	}

	@Override
	protected int getBatchInterval() {
		return 60000;
	}

	@Override
	protected void process(EmailBatchItem item) {
		
	}

	@Override
	protected String getResourceKey() {
		return "email";
	}

	@Override
	public void queueEmail(Realm realm, String subject, String plainText, String html, String replyToName,
			String replyToEmail, RecipientHolder[] recipients, String[] archiveAddresses, Boolean track, Boolean useTemplate) {

	}

}
