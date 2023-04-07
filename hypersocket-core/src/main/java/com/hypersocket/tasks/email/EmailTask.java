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

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.email.EmailAttachment;
import com.hypersocket.email.EmailMessageDeliveryProvider;
import com.hypersocket.email.EmailNotificationBuilder;
import com.hypersocket.email.events.EmailEvent;
import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.html.HtmlTemplateResource;
import com.hypersocket.html.HtmlTemplateResourceRepository;
import com.hypersocket.messagedelivery.MessageDeliveryService;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.MediaType;
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

	private static final String ATTR_PROVIDER = "email.provider";

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
	private MessageDeliveryService messageDeliveryService;

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

	protected void validateEmailField(EmailMessageDeliveryProvider<EmailNotificationBuilder> provider, String field, Map<String, String> parameters, List<TriggerValidationError> invalidAttributes) {
		
		String value = parameters
				.get(ATTR_TO_ADDRESSES);
		
		if(!ResourceUtils.isReplacementVariable(value)) {
			String[] emails = ResourceUtils.explodeValues(value);
			for (String email : emails) {
				if(!ResourceUtils.isReplacementVariable(email)) {
					if (!provider.newBuilder().validate(email)) {
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
		var providerName = parameters.get(ATTR_PROVIDER);

		EmailMessageDeliveryProvider<EmailNotificationBuilder> provider = (EmailMessageDeliveryProvider<EmailNotificationBuilder>) messageDeliveryService.getProviderOrBest(MediaType.EMAIL, providerName, EmailNotificationBuilder.class);

		if(!parameters.containsKey(ATTR_TO_ADDRESSES)
				|| StringUtils.isEmpty(parameters.get(ATTR_TO_ADDRESSES))) {
			invalidAttributes
					.add(new TriggerValidationError(ATTR_TO_ADDRESSES));
		} else {
			validateEmailField(provider, ATTR_TO_ADDRESSES, parameters, invalidAttributes);
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

		
		var subject = processTokenReplacements(
				repository.getValue(task, ATTR_SUBJECT), event, false, false);
		var body = processTokenReplacements(
				repository.getValue(task, ATTR_BODY), event, false, false);
		var bodyHtml = processTokenReplacements(
				repository.getValue(task, ATTR_BODY_HTML), event, false, false);

		var providerName = repository.getValue(task, ATTR_PROVIDER);
		EmailMessageDeliveryProvider<EmailNotificationBuilder> provider = (EmailMessageDeliveryProvider<EmailNotificationBuilder>) messageDeliveryService.getProviderOrBest(MediaType.EMAIL, providerName, EmailNotificationBuilder.class);

		var recipients = ResourceUtils.explodeValues(processTokenReplacements(repository.getValue(task, ATTR_TO_ADDRESSES), event));

		if(log.isInfoEnabled()) {
			log.info(String.format("Sending email named %s to %d receipients", subject, recipients.length));
		}
		
		var attachments = new ArrayList<EmailAttachment>();
	
		
		for(var uuid : repository.getValues(task, ATTR_STATIC_ATTACHMENTS)) {
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
		
		for(var path : repository.getValues(task,  ATTR_DYNAMIC_ATTACHMENTS)) {
			try {
				addFileAttachment(path, attachments, event);
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				return new EmailTaskResult(this, currentRealm, task, e);
			}
		}
		
		var replyToName = processTokenReplacements(repository.getValue(task, "email.replyToName"), event);
		var replyToEmail = processTokenReplacements(repository.getValue(task, "email.replyToEmail"), event);
		var track = repository.getBooleanValue(task, "email.track");
		var archive = repository.getBooleanValue(task, "email.archive");
		var templateStr = repository.getValue(task, "email.htmlTemplate");
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

		var builder = provider.newBuilder(currentRealm);
		builder.subject(subject);
		builder.text(body);
		builder.html(bodyHtml);
		builder.replyToName(replyToName);
		builder.replyToEmail(replyToEmail);
		builder.addRecipientAddresses(recipients);
		builder.archive(archive);
		builder.track(track);
		builder.delay(delay);
		builder.attachments(attachments);
		
		try {
			var result = builder.send();
			return new EmailTaskResult(this, currentRealm, result.getId().orElse(""), task);
		} catch (Exception ex) {
			log.error("Failed to send email", ex);
			return new EmailTaskResult(this, currentRealm, task, ex);
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


	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}
	
	@Override
	public boolean isSystem() {
		return false;
	}

}
