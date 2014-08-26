package com.hypersocket.triggers.actions.email;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.hypersocket.triggers.AbstractAction;
import com.hypersocket.triggers.ActionResult;
import com.hypersocket.triggers.TriggerAction;
import com.hypersocket.triggers.TriggerActionProvider;
import com.hypersocket.triggers.TriggerResourceService;
import com.hypersocket.triggers.TriggerResourceServiceImpl;
import com.hypersocket.triggers.TriggerValidationError;
import com.hypersocket.triggers.TriggerValidationException;

@Component
public class EmailTriggerAction extends AbstractAction implements
		TriggerActionProvider {

	private static Logger log = LoggerFactory
			.getLogger(EmailTriggerAction.class);

	public static final String ACTION_RESOURCE_KEY = "emailAction";

	public static final String ATTR_TO_ADDRESSES = "email.to";
	public static final String ATTR_CC_ADDRESSES = "email.cc";
	public static final String ATTR_BCC_ADDRESSES = "email.bcc";
	public static final String ATTR_SUBJECT = "email.subject";
	public static final String ATTR_FORMAT = "email.format";
	public static final String ATTR_BODY = "email.body";

	private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

	@Autowired
	TriggerResourceService triggerService;

	@Autowired
	EmailNotificationService emailService;

	@Autowired
	EmailTriggerActionRepository repository;

	@Autowired
	EventService eventService;

	@PostConstruct
	private void postConstruct() {
		triggerService.registerActionProvider(this);

		eventService.registerEvent(EmailActionResult.class,
				TriggerResourceServiceImpl.RESOURCE_BUNDLE);
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
	public Collection<PropertyCategory> getPropertiesForAction(
			TriggerAction action) {
		return repository.getPropertyCategories(action);
	}

	@Override
	public String[] getResourceKeys() {
		return new String[] { "emailAction" };
	}

	@Override
	public String[] getRequiredAttributes() {
		return new String[] { ATTR_TO_ADDRESSES, ATTR_SUBJECT, ATTR_BODY,
				ATTR_FORMAT };
	}

	@Override
	public void validate(TriggerAction action, Map<String, String> parameters)
			throws TriggerValidationException {

		List<TriggerValidationError> invalidAttributes = new ArrayList<TriggerValidationError>();

		if (!parameters.containsKey(ATTR_TO_ADDRESSES)
				|| StringUtils.isEmpty(parameters.get(ATTR_TO_ADDRESSES))) {
			invalidAttributes
					.add(new TriggerValidationError(ATTR_TO_ADDRESSES));
		} else {
			String[] emails = repository.explodeValues(parameters
					.get(ATTR_TO_ADDRESSES));
			for (String email : emails) {
				if (!validateEmailAddress(email)) {
					invalidAttributes.add(new TriggerValidationError(
							ATTR_TO_ADDRESSES, email));
				}
			}
		}

		String[] emails = repository.explodeValues(parameters
				.get(ATTR_CC_ADDRESSES));
		for (String email : emails) {
			if (!validateEmailAddress(email)) {
				invalidAttributes.add(new TriggerValidationError(
						ATTR_CC_ADDRESSES, email));
			}
		}

		emails = repository.explodeValues(parameters.get(ATTR_BCC_ADDRESSES));
		for (String email : emails) {
			if (!validateEmailAddress(email)) {
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

	private boolean validateEmailAddress(String email) {
		return true;
	}

	@Override
	public ActionResult execute(TriggerAction action, SystemEvent event)
			throws TriggerValidationException {

		String subject = processTokenReplacements(
				repository.getValue(action, ATTR_SUBJECT), event);
		String body = processTokenReplacements(
				repository.getValue(action, ATTR_BODY), event);
		List<Recipient> recipients = new ArrayList<Recipient>();

		String to = populateEmailList(action, ATTR_TO_ADDRESSES, recipients,
				RecipientType.TO, event);
		String cc = populateEmailList(action, ATTR_CC_ADDRESSES, recipients,
				RecipientType.TO, event);
		String bcc = populateEmailList(action, ATTR_BCC_ADDRESSES, recipients,
				RecipientType.TO, event);

		try {
			emailService.sendPlainEmail(subject, body,
					recipients.toArray(new Recipient[0]));

			return new EmailActionResult(this, action.getTrigger().getRealm(),
					action.getName(), action.getTrigger().getName(), subject, body, to, cc, bcc);

		} catch (Exception ex) {
			log.error("Failed to send email", ex);
			return new EmailActionResult(this, ex, action.getTrigger()
					.getRealm(), action.getName(), action.getTrigger().getName(), subject, body, to, cc, bcc);
		}
	}

	private String populateEmailList(TriggerAction action,
			String attributeName, List<Recipient> recipients,
			RecipientType type, SystemEvent event)
			throws TriggerValidationException {

		StringBuffer ret = new StringBuffer();
		String[] emails = repository.explodeValues(processTokenReplacements(
				repository.getValue(action, attributeName), event));
		for (String email : emails) {
			if (ret.length() > 0) {
				ret.append(", ");
			}
			ret.append(email);
			String[] rec = getEmailName(email);
			recipients.add(new Recipient(rec[0], rec[1], type));
		}

		return ret.toString();
	}

	private String[] getEmailName(String val) throws TriggerValidationException {
		Pattern p = Pattern.compile("(.*?)<([^>]+)>\\s*,?");

		Matcher m = p.matcher(val);

		if (m.find()) {
			String name = m.group(1).replaceAll("[\\n\\r]+", "");
			String email = m.group(2).replaceAll("[\\n\\r]+", "");

			if (Pattern.matches(EMAIL_PATTERN, email)) {
				return new String[] { name, email };
			} else {
				throw new TriggerValidationException(email
						+ " is not a valid email address");
			}
		}

		if (Pattern.matches(EMAIL_PATTERN, val)) {
			return new String[] { "", val };
		}

		throw new TriggerValidationException(val
				+ " is not a valid email address");
	}

	@Override
	public ResourceTemplateRepository getRepository() {
		return repository;
	}

}
