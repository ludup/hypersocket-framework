package com.hypersocket.email;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.codemonkey.simplejavamail.MailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.batch.BatchProcessingItemRepository;
import com.hypersocket.batch.BatchProcessingServiceImpl;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.triggers.ValidationException;
import com.hypersocket.upload.FileUpload;
import com.hypersocket.upload.FileUploadService;

public class EmailBatchServiceImpl extends BatchProcessingServiceImpl<EmailBatchItem> implements EmailBatchService {

	static Logger log = LoggerFactory.getLogger(EmailBatchServiceImpl.class);

	@Autowired
	EmailBatchRepository repository;

	@Autowired
	EmailNotificationService emailService;

	@Autowired
	FileUploadService uploadService;

	@Override
	protected BatchProcessingItemRepository<EmailBatchItem> getRepository() {
		return repository;
	}

	@Override
	protected int getBatchInterval() {
		return 60000;
	}

	@Override
	protected boolean process(EmailBatchItem item) {

		try {

			if(item.getSchedule()!=null && item.getSchedule().after(new Date())) {
				return false;
			}
			
			List<EmailAttachment> attachments = new ArrayList<EmailAttachment>();
			for (String uuid : ResourceUtils.explodeValues(item.getAttachments())) {
				try {
					FileUpload upload = uploadService.getFileUpload(uuid);
					attachments.add(new EmailAttachment(upload.getFileName(), uploadService.getContentType(uuid),
							uploadService.getInputStream(uuid)));
				} catch (ResourceNotFoundException | IOException e) {
					log.error(String.format("Unable to locate upload %s", uuid), e);
				}
			}

			emailService.sendEmail(item.getRealm(), item.getSubject(), item.getText(), item.getHtml(),
					item.getReplyToName(), item.getReplyToEmail(),
					new RecipientHolder[] { new RecipientHolder(item.getToName(), item.getToEmail()) }, item.getTrack(),
					50, attachments.toArray(new EmailAttachment[0]));

			return true;
		} catch (MailException | AccessDeniedException | ValidationException e) {
			throw new IllegalStateException(e.getMessage(), e);
		} 

	}

	@Override
	protected String getResourceKey() {
		return "email";
	}

	@Override
	public void queueEmail(Realm realm, String subject, String body, String html, String replyToName,
			String replyToEmail, String name, String email, Boolean track, String attachments) throws ResourceException {
		scheduleEmail(realm, subject, body, html, replyToName, replyToEmail, name, email, track, attachments, null);
	}

	@Override
	public void scheduleEmail(Realm realm, String subject, String body, String html, String replyToName,
			String replyToEmail, String name, String email, Boolean track, String attachments, Date schedule) throws ResourceException {

		EmailBatchItem item = new EmailBatchItem();
		item.setRealm(realm);
		item.setSubject(subject);
		item.setText(body);
		item.setHtml(html);
		item.setReplyToName(replyToName);
		item.setReplyToEmail(replyToEmail);
		item.setToName(name);
		item.setToEmail(email);
		item.setTrack(track);
		item.setAttachments(attachments);
		if(schedule!=null) {
			item.setSchedule(schedule);
		}
		
		repository.saveResource(item);
	}

}
