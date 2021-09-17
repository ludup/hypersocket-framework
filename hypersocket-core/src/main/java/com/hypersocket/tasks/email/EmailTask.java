package com.hypersocket.tasks.email;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.mail.Message.RecipientType;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.email.EmailAttachment;
import com.hypersocket.email.EmailNotificationService;
import com.hypersocket.email.RecipientHolder;
import com.hypersocket.email.events.EmailEvent;
import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.html.HtmlTemplateResource;
import com.hypersocket.html.HtmlTemplateResourceRepository;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.TaskProviderServiceImpl;
import com.hypersocket.tasks.TaskResult;
import com.hypersocket.triggers.TriggerResourceServiceImpl;
import com.hypersocket.triggers.TriggerValidationError;
import com.hypersocket.triggers.ValidationException;
import com.hypersocket.upload.FileUpload;
import com.hypersocket.upload.FileUploadService;
import com.hypersocket.util.CloseOnEOFInputStream;

@Component
public class EmailTask extends AbstractTaskProvider {

	private static Logger log = LoggerFactory
			.getLogger(EmailTask.class);

	public static final String ACTION_RESOURCE_KEY = "sendEmail";

	public static final String ATTR_TO_ADDRESSES = "email.to";
	public static final String ATTR_SUBJECT = "email.subject";
	public static final String ATTR_FORMAT = "email.format";
	public static final String ATTR_BODY = "email.body";
	public static final String ATTR_BODY_HTML = "email.bodyHtml";
	public static final String ATTR_STATIC_ATTACHMENTS = "attach.static";
	public static final String ATTR_DYNAMIC_ATTACHMENTS = "attach.dynamic";
	public static final String ATTR_EVENT_SOURCE = "attach.event";
	public static final String ATTR_EVENT_SOURCE_TYPE = "attach.eventSourceType";

	@Autowired
	private EmailNotificationService emailService;

	@Autowired
	private EmailTaskRepository repository;

	@Autowired
	private EventService eventService;

	@Autowired
	private TaskProviderService taskService; 
	
	@Autowired
	private FileUploadService uploadService; 
	
	@Autowired
	private HtmlTemplateResourceRepository htmlTemplateRepository;
	
	@PostConstruct
	private void postConstruct() {
		taskService.registerTaskProvider(this);

		eventService.registerEvent(EmailEvent.class,
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
	public TaskResult execute(Task task, Realm currentRealm, List<SystemEvent> event)
			throws ValidationException {

		
		String subject = processTokenReplacements(
				repository.getValue(task, ATTR_SUBJECT), event, false, false);
		String body = processTokenReplacements(
				repository.getValue(task, ATTR_BODY), event, false, false);
		String bodyHtml = processTokenReplacements(
				repository.getValue(task, ATTR_BODY_HTML), event, false, false);
		List<RecipientHolder> recipients = new ArrayList<RecipientHolder>();

		populateEmailList(task, ATTR_TO_ADDRESSES, recipients,
				RecipientType.TO, event);

		if(log.isInfoEnabled()) {
			log.info(String.format("Sending email named %s to %d receipients", subject, recipients.size()));
		}
		
		List<EmailAttachment> attachments = new ArrayList<EmailAttachment>();
	
		
		for(String uuid : repository.getValues(task, ATTR_STATIC_ATTACHMENTS)) {
			try {
				addUUIDAttachment(uuid, new String[0], attachments);
			} catch (ResourceException e) {
				log.error("Failed to get upload file", e);
				return new EmailTaskResult(this, currentRealm, task, e);
			} catch (IOException e) {
				log.error("Failed to get upload file", e);
				return new EmailTaskResult(this, currentRealm, task, e);
			}
		}
		
		for(String path : repository.getValues(task,  ATTR_DYNAMIC_ATTACHMENTS)) {
			try {
				addFileAttachment(path, attachments, event);
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				return new EmailTaskResult(this, currentRealm, task, e);
			}
		}
		
		String replyToName = processTokenReplacements(repository.getValue(task, "email.replyToName"), event);
		String replyToEmail = processTokenReplacements(repository.getValue(task, "email.replyToEmail"), event);
		boolean track = repository.getBooleanValue(task, "email.track");
		boolean archive = repository.getBooleanValue(task, "email.archive");
		String templateStr = repository.getValue(task, "email.htmlTemplate");
		HtmlTemplateResource template = null;
		if(StringUtils.isNotBlank(templateStr) && StringUtils.isNumeric(templateStr)) {
			template = htmlTemplateRepository.getResourceById(Long.parseLong(templateStr));
			
			if(template!=null && StringUtils.isNotBlank(template.getHtml())) {
				
				Document doc = Jsoup.parse(template.getHtml());
				Elements elements = doc.select(template.getContentSelector());
				elements.first().append(bodyHtml);
				bodyHtml = doc.toString();		
				bodyHtml = processTokenReplacements(bodyHtml, event, false, false);
			}
		}
		
		int delay = repository.getIntValue(task, "email.delay");
		
		emailService.enableSynchronousEmail();
		try {
			emailService.sendEmail(currentRealm, subject, body, bodyHtml,
					replyToName, replyToEmail, 
					recipients.toArray(new RecipientHolder[0]), archive, track, delay, null, attachments.toArray(new EmailAttachment[0]));

			return new EmailTaskResult(this, currentRealm, task);
		} catch (Exception ex) {
			log.error("Failed to send email", ex);
			return new EmailTaskResult(this, currentRealm, task, ex);
		} finally {
			emailService.disableSynchronousEmail();
		}
		
		
	}
	
	private void addUUIDAttachment(String uuid, String[] typesRequired, List<EmailAttachment> attachments)
			throws ResourceNotFoundException, FileNotFoundException, IOException {
		FileUpload upload = uploadService.getFileUpload(uuid);
		if (typesRequired.length == 0) {
			attachments.add(new EmailAttachment(upload.getFileName(), uploadService.getContentType(uuid)) {
				@Override
				public InputStream getInputStream() throws IOException {
					return new CloseOnEOFInputStream(uploadService.getInputStream(uuid));
				}
			});
		} else {
			for (String typeRequired : typesRequired) {
				if (upload.getType().equalsIgnoreCase(typeRequired)) {
					attachments.add(new EmailAttachment(upload.getFileName(), uploadService.getContentType(uuid)) {
						@Override
						public InputStream getInputStream() throws IOException {
							return new CloseOnEOFInputStream(uploadService.getInputStream(getName()));
						}
					});
				}
			}
		}
	}
	
	private void addFileAttachment(String path, List<EmailAttachment> attachments, List<SystemEvent> event) throws FileNotFoundException, UnsupportedEncodingException {
		
		path = URLDecoder.decode(path, "UTF-8");
		String filename = null;
		String filepath;

		if(ResourceUtils.isNamePair(path)) {
			filename = ResourceUtils.getNamePairKey(path);
			filename = processTokenReplacements(filename, event);
			
			filepath = ResourceUtils.getNamePairValue(path);
		} else {
			filepath = path;
		}
		
		filepath = processTokenReplacements(filepath, event);
		File file = new File(filepath);
		if(!file.exists()) {
			throw new FileNotFoundException(filepath + " does not exist");
		}
		
		if(filename==null) {
			filename = file.getName();
		}
		
		attachments.add(new EmailAttachment(filename, uploadService.getContentType(file)) {
			@Override
			public InputStream getInputStream() throws IOException {
				return new CloseOnEOFInputStream(new FileInputStream(file));
			}
		});

	}
	public String getResultResourceKey() {
		return EmailEvent.EVENT_RESOURCE_KEY;
	}

	private String populateEmailList(Task task,
			String attributeName, List<RecipientHolder> recipients,
			RecipientType type, List<SystemEvent> event)
			throws ValidationException {

		String value = processTokenReplacements(repository.getValue(task, attributeName), event);
		String[] emails = ResourceUtils.explodeValues(value);
		return emailService.populateEmailList(emails, recipients, type);
	}

	

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}
	
	@Override
	public boolean isSystem() {
		return false;
	}

}
