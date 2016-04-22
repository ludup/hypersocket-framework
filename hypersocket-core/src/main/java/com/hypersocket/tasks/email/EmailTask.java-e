package com.hypersocket.tasks.email;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.mail.Message.RecipientType;

import org.apache.commons.lang3.StringUtils;
import org.codemonkey.simplejavamail.Recipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.email.EmailAttachment;
import com.hypersocket.email.EmailNotificationService;
import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.TaskResult;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.TaskProviderServiceImpl;
import com.hypersocket.triggers.TriggerResourceService;
import com.hypersocket.triggers.TriggerResourceServiceImpl;
import com.hypersocket.triggers.TriggerValidationError;
import com.hypersocket.triggers.ValidationException;
import com.hypersocket.upload.FileUpload;
import com.hypersocket.upload.FileUploadService;
import com.hypersocket.utils.HypersocketUtils;

@Component
public class EmailTask extends AbstractTaskProvider {

	private static Logger log = LoggerFactory
			.getLogger(EmailTask.class);

	public static final String ACTION_RESOURCE_KEY = "sendEmail";

	public static final String ATTR_TO_ADDRESSES = "email.to";
	public static final String ATTR_CC_ADDRESSES = "email.cc";
	public static final String ATTR_BCC_ADDRESSES = "email.bcc";
	public static final String ATTR_SUBJECT = "email.subject";
	public static final String ATTR_FORMAT = "email.format";
	public static final String ATTR_BODY = "email.body";
	public static final String ATTR_BODY_HTML = "email.bodyHtml";
	public static final String ATTR_STATIC_ATTACHMENTS = "attach.static";
	public static final String ATTR_DYNAMIC_ATTACHMENTS = "attach.dynamic";
	public static final String ATTR_EVENT_SOURCE = "attach.event";
	public static final String ATTR_EVENT_SOURCE_TYPE = "attach.eventSourceType";
	
	@Autowired
	TriggerResourceService triggerService;

	@Autowired
	EmailNotificationService emailService;

	@Autowired
	EmailTaskRepository repository;

	@Autowired
	EventService eventService;

	@Autowired
	TaskProviderService taskService; 
	
	@Autowired
	FileUploadService uploadService; 
	
	@PostConstruct
	private void postConstruct() {
		taskService.registerTaskProvider(this);

		eventService.registerEvent(EmailTaskResult.class,
				TaskProviderServiceImpl.RESOURCE_BUNDLE);
	}

	@Override
	public String getResourceBundle() {
		return TriggerResourceServiceImpl.RESOURCE_BUNDLE;
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate(Task task) {
		return repository.getPropertyCategories(task);
	}

	@Override
	public Collection<PropertyCategory> getProperties(
			Task task) {
		return repository.getPropertyCategories(task);
	}

	@Override
	public String[] getResourceKeys() {
		return new String[] { "sendEmail" };
	}

	protected void validateEmailField(String field, Map<String, String> parameters, List<TriggerValidationError> invalidAttributes) {
		
		String value = parameters
				.get(ATTR_TO_ADDRESSES);
		
		if(!ResourceUtils.isReplacementVariable(value)) {
			String[] emails = ResourceUtils.explodeValues(value);
			for (String email : emails) {
				if(!ResourceUtils.isReplacementVariable(email)) {
					if (!emailService.validateEmailAddress(email)) {
						invalidAttributes.add(new TriggerValidationError(
								ATTR_TO_ADDRESSES, email));
					}
				}
			}
		}
	}
	
	@Override
	public void validate(Task task, Map<String, String> parameters)
			throws ValidationException {

		List<TriggerValidationError> invalidAttributes = new ArrayList<TriggerValidationError>();

		if(!parameters.containsKey(ATTR_TO_ADDRESSES)
				|| StringUtils.isEmpty(parameters.get(ATTR_TO_ADDRESSES))) {
			invalidAttributes
					.add(new TriggerValidationError(ATTR_TO_ADDRESSES));
		} else {
			validateEmailField(ATTR_TO_ADDRESSES, parameters, invalidAttributes);
		}

		validateEmailField(ATTR_CC_ADDRESSES, parameters, invalidAttributes);
		validateEmailField(ATTR_BCC_ADDRESSES, parameters, invalidAttributes);
		

		if(!parameters.containsKey(ATTR_SUBJECT)
				|| StringUtils.isEmpty(parameters.get(ATTR_SUBJECT))) {
			invalidAttributes.add(new TriggerValidationError(ATTR_SUBJECT));
		}

		if(!parameters.containsKey(ATTR_BODY)
				|| StringUtils.isEmpty(parameters.get(ATTR_BODY))) {
			invalidAttributes.add(new TriggerValidationError(ATTR_BODY));
		}

		if(!parameters.containsKey(ATTR_FORMAT)
				|| StringUtils.isEmpty(parameters.get(ATTR_FORMAT))) {
			invalidAttributes.add(new TriggerValidationError(ATTR_FORMAT));
		}
	}

	@Override
	public TaskResult execute(Task task, Realm currentRealm, SystemEvent event)
			throws ValidationException {

		String subject = processTokenReplacements(
				repository.getValue(task, ATTR_SUBJECT), event);
		String body = processTokenReplacements(
				repository.getValue(task, ATTR_BODY), event);
		String bodyHtml = processTokenReplacements(
				repository.getValue(task, ATTR_BODY_HTML), event);
		List<Recipient> recipients = new ArrayList<Recipient>();

		String to = populateEmailList(task, ATTR_TO_ADDRESSES, recipients,
				RecipientType.TO, event);
		String cc = populateEmailList(task, ATTR_CC_ADDRESSES, recipients,
				RecipientType.CC, event);
		String bcc = populateEmailList(task, ATTR_BCC_ADDRESSES, recipients,
				RecipientType.BCC, event);

		List<EmailAttachment> attachments = new ArrayList<EmailAttachment>();
		
		if(repository.getBooleanValue(task, ATTR_EVENT_SOURCE) && event instanceof EmailAttachmentSource) {
			
			String[] attachmentType = ResourceUtils.explodeValues(
					repository.getValue(task, ATTR_EVENT_SOURCE_TYPE));
			String[] eventAttachments = ((EmailAttachmentSource)event).getEmailAttachments();
			for(String attachment : eventAttachments) {
				try {
					if(HypersocketUtils.isUUID(attachment)) {
						addUUIDAttachment(attachment, attachmentType, attachments);
					} else {
						addFileAttachment(attachment, attachments, event);
					}
				} catch (Exception e) {
					return new EmailTaskResult(this, e, currentRealm, task, subject, body, to, cc, bcc);
				}
			}
			
		}
		
		for(String uuid : repository.getValues(task, ATTR_STATIC_ATTACHMENTS)) {
			try {
				addUUIDAttachment(uuid, new String[0], attachments);
			} catch (ResourceException e) {
				log.error("Failed to get upload file", e);
				return new EmailTaskResult(this, e, currentRealm, task, subject, body, to, cc, bcc);
			} catch (IOException e) {
				log.error("Failed to get upload file", e);
				return new EmailTaskResult(this, e, currentRealm, task, subject, body, to, cc, bcc);
			}
		}
		
		for(String path : repository.getValues(task,  ATTR_DYNAMIC_ATTACHMENTS)) {
			try {
				addFileAttachment(path, attachments, event);
			} catch (FileNotFoundException e) {
				return new EmailTaskResult(this, e, currentRealm, task, subject, body, to, cc, bcc);
			}
		}
		
		String replyToName = processTokenReplacements(repository.getValue(task, "email.replyToName"), event);
		String replyToEmail = processTokenReplacements(repository.getValue(task, "email.replyToEmail"), event);
		
		try {
			emailService.sendEmail(currentRealm, subject, body, bodyHtml,
					replyToName, replyToEmail, 
					recipients.toArray(new Recipient[0]), attachments.toArray(new EmailAttachment[0]));

			return new EmailTaskResult(this, task.getRealm(),
					task, subject, body, to, cc, bcc);

		} catch (Exception ex) {
			log.error("Failed to send email", ex);
			return new EmailTaskResult(this, ex, currentRealm, task, subject, body, to, cc, bcc);
		}
	}
	
	private void addUUIDAttachment(String uuid, String[] typesRequired, List<EmailAttachment> attachments) throws ResourceNotFoundException, FileNotFoundException, IOException {
		FileUpload upload = uploadService.getFileByUuid(uuid);	
		if(typesRequired.length==0) {
			attachments.add(new EmailAttachment(upload.getFileName(), 
					uploadService.getContentType(uuid), 
					uploadService.getFile(uuid)));
		} else {
			for(String typeRequired : typesRequired) {
				if(upload.getType().equalsIgnoreCase(typeRequired)) {
					attachments.add(new EmailAttachment(upload.getFileName(), 
						uploadService.getContentType(uuid), 
						uploadService.getFile(uuid)));
				}
			}
		}
	}
	
	private void addFileAttachment(String path, List<EmailAttachment> attachments, SystemEvent event) throws FileNotFoundException {
		
		String filename = ResourceUtils.getNamePairKey(path);
		String filepath = ResourceUtils.getNamePairValue(path);
		
		filename = processTokenReplacements(filename, event);
		filepath = processTokenReplacements(filepath, event);
		File file = new File(filepath);
		if(!file.exists()) {
			throw new FileNotFoundException(filepath + " does not exist");
		}
		
		attachments.add(new EmailAttachment(filename, uploadService.getContentType(file), file));

	}
	public String[] getResultResourceKeys() {
		return new String[] { EmailTaskResult.EVENT_RESOURCE_KEY };
	}

	private String populateEmailList(Task task,
			String attributeName, List<Recipient> recipients,
			RecipientType type, SystemEvent event)
			throws ValidationException {

		String value = processTokenReplacements(repository.getValue(task, attributeName), event);
		String[] emails = ResourceUtils.explodeValues(value);
		return emailService.populateEmailList(emails, recipients, type);
	}

	

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}

}
