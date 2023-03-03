package com.hypersocket.email;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.simplejavamail.MailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.batch.BatchProcessingItemRepository;
import com.hypersocket.batch.BatchProcessingServiceImpl;
import com.hypersocket.messagedelivery.MessageDeliveryException;
import com.hypersocket.messagedelivery.MessageDeliveryService;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.MediaType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.upload.FileUpload;
import com.hypersocket.upload.FileUploadService;

@Service
public class EmailBatchServiceImpl extends BatchProcessingServiceImpl<EmailBatchItem> implements EmailBatchService {

	static Logger log = LoggerFactory.getLogger(EmailBatchServiceImpl.class);

	@Autowired
	private EmailBatchRepository repository;

	@Autowired
	private MessageDeliveryService messageDeliveryService;

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

			@SuppressWarnings("unchecked")
			var provider = StringUtils.isNotBlank(item.getProvider()) ? 
						(EmailMessageDeliveryProvider<EmailNotificationBuilder>)messageDeliveryService.getProvider(item.getProvider()) : 
							messageDeliveryService.getBestProvider(MediaType.EMAIL, EmailNotificationBuilder.class);
			var builder = provider.newBuilder(realmService.getRealmById(item.getRealm()));
			builder.subject(item.getSubject());
			builder.text(item.getText());
			builder.html(item.getHtml());
			builder.replyToName(item.getReplyToName());
			builder.replyToEmail(item.getReplyToEmail());
			builder.recipient(RecipientHolder.ofNameAndAddress(item.getToName(), item.getToEmail()));
			builder.archive(item.getArchive());
			builder.track(item.getTrack());
			builder.delay(5);
			builder.context(item.getContext());
			builder.attachments(attachments);
			builder.send();

			
		} catch (MailException | MessageDeliveryException e) {
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
	public void queueEmail(Realm realm, Optional<String> provider, String subject, String body, String html, String replyToName,
			String replyToEmail, String name, String email, Boolean archive, Boolean track, String attachments, String context)
			throws ResourceException {
		scheduleEmail(realm, provider, subject, body, html, replyToName, replyToEmail, name, email, archive, track, attachments, null, context);
	}

	@Override
	public void scheduleEmail(Realm realm, Optional<String> provider,  String subject, String body, String html, String replyToName,
			String replyToEmail, String name, String email, Boolean archive, Boolean track, String attachments, Date schedule, String context)
			throws ResourceException {

		EmailBatchItem item = new EmailBatchItem();
		item.setRealm(realm.getId());
		item.setContext(context);
		item.setProvider(provider.orElse(""));
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
