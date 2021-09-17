package com.hypersocket.email;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.simplejavamail.MailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.batch.BatchProcessingItemRepository;
import com.hypersocket.batch.BatchProcessingServiceImpl;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.triggers.ValidationException;
import com.hypersocket.upload.FileUpload;
import com.hypersocket.upload.FileUploadService;

@Service
public class EmailBatchServiceImpl extends BatchProcessingServiceImpl<EmailBatchItem> implements EmailBatchService {

	static Logger log = LoggerFactory.getLogger(EmailBatchServiceImpl.class);

	@Autowired
	private EmailBatchRepository repository;

	@Autowired
	private EmailNotificationService emailService;

	@Autowired
	private FileUploadService uploadService;

	@Autowired
	private RealmService realmService;

	@Override
	protected BatchProcessingItemRepository<EmailBatchItem> getRepository() {
		return repository;
	}

	@Override
	protected int getBatchInterval() {
		return 60000;
	}

	@Override
	protected boolean process(EmailBatchItem item) throws Exception {

		try {

			if (item.getSchedule() != null && item.getSchedule().after(new Date())) {
				return false;
			}

			List<EmailAttachment> attachments = new ArrayList<EmailAttachment>();
			for (String uuid : ResourceUtils.explodeValues(item.getAttachments())) {
				try {
					FileUpload upload = uploadService.getFileUpload(uuid);
					attachments.add(new EmailAttachment(upload.getFileName(), uploadService.getContentType(uuid)) {
						@Override
						public InputStream getInputStream() throws IOException {
							return uploadService.getInputStream(uuid);
						}
						
					});
				} catch (ResourceNotFoundException | IOException e) {
					log.error(String.format("Unable to locate upload %s", uuid), e);
				}
			}

			emailService.sendEmail(realmService.getRealmById(item.getRealm()), item.getSubject(), item.getText(), item.getHtml(),
					item.getReplyToName(), 
					item.getReplyToEmail(),
					new RecipientHolder[] { new RecipientHolder(item.getToName(), item.getToEmail()) }, 
					item.getArchive(), 
					item.getTrack(),
					5, item.getContext(), attachments.toArray(new EmailAttachment[0]));

			
		} catch (MailException | AccessDeniedException | ValidationException e) {
			log.error("I could not send an email in realm {} to user {} with subject {}",
					item.getRealm(),
					item.getToEmail(), 
					item.getSubject(),
					e);
			throw e;
		}

		return true;
	}

	@Override
	protected String getResourceKey() {
		return "email";
	}

	@Override
	public void queueEmail(Realm realm, String subject, String body, String html, String replyToName,
			String replyToEmail, String name, String email, Boolean archive, Boolean track, String attachments, String context)
			throws ResourceException {
		scheduleEmail(realm, subject, body, html, replyToName, replyToEmail, name, email, archive, track, attachments, null, context);
	}

	@Override
	public void scheduleEmail(Realm realm, String subject, String body, String html, String replyToName,
			String replyToEmail, String name, String email, Boolean archive, Boolean track, String attachments, Date schedule, String context)
			throws ResourceException {

		EmailBatchItem item = new EmailBatchItem();
		item.setRealm(realm.getId());
		item.setContext(context);
		item.setSubject(subject);
		item.setText(body);
		item.setHtml(html);
		item.setReplyToName(replyToName);
		item.setReplyToEmail(replyToEmail);
		item.setToName(name);
		item.setToEmail(email);
		item.setTrack(track);
		item.setAttachments(attachments);
		item.setArchive(archive);
		if (schedule != null) {
			item.setSchedule(schedule);
		}

		repository.merge(item);
	}

	@Override
	protected boolean onProcessFailure(EmailBatchItem item, Throwable exception) {
		if(exception instanceof MailException && exception.getClass().getSimpleName().equals("MailSenderException")) {
			/* Retry on sender exceptions */
			return false;
		}
		return super.onProcessFailure(item, exception);
	}

}
