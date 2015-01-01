package com.hypersocket.tasks.email;

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

import com.hypersocket.email.EmailNotificationService;
import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.tasks.AbstractTaskProvider;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskProvider;
import com.hypersocket.tasks.TaskProviderService;
import com.hypersocket.tasks.TaskProviderServiceImpl;
import com.hypersocket.triggers.TaskResult;
import com.hypersocket.triggers.TriggerResourceService;
import com.hypersocket.triggers.TriggerResourceServiceImpl;
import com.hypersocket.triggers.TriggerValidationError;
import com.hypersocket.triggers.ValidationException;

@Component
public class EmailTask extends AbstractTaskProvider implements
		TaskProvider {

	private static Logger log = LoggerFactory
			.getLogger(EmailTask.class);

	public static final String ACTION_RESOURCE_KEY = "emailAction";

	public static final String ATTR_TO_ADDRESSES = "email.to";
	public static final String ATTR_CC_ADDRESSES = "email.cc";
	public static final String ATTR_BCC_ADDRESSES = "email.bcc";
	public static final String ATTR_SUBJECT = "email.subject";
	public static final String ATTR_FORMAT = "email.format";
	public static final String ATTR_BODY = "email.body";

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
	@PostConstruct
	private void postConstruct() {
		taskService.registerActionProvider(this);

		eventService.registerEvent(EmailTaskResult.class,
				TaskProviderServiceImpl.RESOURCE_BUNDLE);
	}

	@Override
	public String getResourceBundle() {
		return TriggerResourceServiceImpl.RESOURCE_BUNDLE;
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate() {
		return repository.getPropertyCategories(null);
	}

	@Override
	public Collection<PropertyCategory> getProperties(
			Task task) {
		return repository.getPropertyCategories(task);
	}

	@Override
	public String[] getResourceKeys() {
		return new String[] { "emailAction" };
	}

	@Override
	public void validate(Task task, Map<String, String> parameters)
			throws ValidationException {

		List<TriggerValidationError> invalidAttributes = new ArrayList<TriggerValidationError>();

		if (!parameters.containsKey(ATTR_TO_ADDRESSES)
				|| StringUtils.isEmpty(parameters.get(ATTR_TO_ADDRESSES))) {
			invalidAttributes
					.add(new TriggerValidationError(ATTR_TO_ADDRESSES));
		} else {
			String[] emails = ResourceUtils.explodeValues(parameters
					.get(ATTR_TO_ADDRESSES));
			for (String email : emails) {
				if (!emailService.validateEmailAddress(email)) {
					invalidAttributes.add(new TriggerValidationError(
							ATTR_TO_ADDRESSES, email));
				}
			}
		}

		String[] emails = ResourceUtils.explodeValues(parameters
				.get(ATTR_CC_ADDRESSES));
		for (String email : emails) {
			if (!emailService.validateEmailAddress(email)) {
				invalidAttributes.add(new TriggerValidationError(
						ATTR_CC_ADDRESSES, email));
			}
		}

		emails = ResourceUtils.explodeValues(parameters.get(ATTR_BCC_ADDRESSES));
		for (String email : emails) {
			if (!emailService.validateEmailAddress(email)) {
				invalidAttributes.add(new TriggerValidationError(
						ATTR_BCC_ADDRESSES, email));
			}
		}

		if (!parameters.containsKey(ATTR_SUBJECT)
				|| StringUtils.isEmpty(parameters.get(ATTR_SUBJECT))) {
			invalidAttributes.add(new TriggerValidationError(ATTR_SUBJECT));
		}

		if (!parameters.containsKey(ATTR_BODY)
				|| StringUtils.isEmpty(parameters.get(ATTR_BODY))) {
			invalidAttributes.add(new TriggerValidationError(ATTR_BODY));
		}

		if (!parameters.containsKey(ATTR_FORMAT)
				|| StringUtils.isEmpty(parameters.get(ATTR_FORMAT))) {
			invalidAttributes.add(new TriggerValidationError(ATTR_FORMAT));
		}
	}

	@Override
	public TaskResult execute(Task task, SystemEvent event)
			throws ValidationException {

		String subject = processTokenReplacements(
				repository.getValue(task, ATTR_SUBJECT), event);
		String body = processTokenReplacements(
				repository.getValue(task, ATTR_BODY), event);
		List<Recipient> recipients = new ArrayList<Recipient>();

		String to = populateEmailList(task, ATTR_TO_ADDRESSES, recipients,
				RecipientType.TO, event);
		String cc = populateEmailList(task, ATTR_CC_ADDRESSES, recipients,
				RecipientType.CC, event);
		String bcc = populateEmailList(task, ATTR_BCC_ADDRESSES, recipients,
				RecipientType.BCC, event);

		try {
			emailService.sendPlainEmail(subject, body,
					recipients.toArray(new Recipient[0]));

			return new EmailTaskResult(this, task.getRealm(),
					task, subject, body, to, cc, bcc);

		} catch (Exception ex) {
			log.error("Failed to send email", ex);
			return new EmailTaskResult(this, ex, task
					.getRealm(), task, subject, body, to, cc, bcc);
		}
	}

	private String populateEmailList(Task task,
			String attributeName, List<Recipient> recipients,
			RecipientType type, SystemEvent event)
			throws ValidationException {

		String[] emails = ResourceUtils.explodeValues(processTokenReplacements(
				repository.getValue(task, attributeName), event));
		return emailService.populateEmailList(emails, recipients, type);
	}

	

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}

	@Override
	public boolean supportsAutomation() {
		return true;
	}

}
