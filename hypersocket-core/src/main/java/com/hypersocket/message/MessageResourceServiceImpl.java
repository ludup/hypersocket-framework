package com.hypersocket.message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.mail.Message.RecipientType;

import org.codemonkey.simplejavamail.MailException;
import org.codemonkey.simplejavamail.Recipient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.config.ConfigurationService;
import com.hypersocket.email.EmailAttachment;
import com.hypersocket.email.EmailNotificationService;
import com.hypersocket.email.RecipientHolder;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.message.events.MessageResourceCreatedEvent;
import com.hypersocket.message.events.MessageResourceDeletedEvent;
import com.hypersocket.message.events.MessageResourceEvent;
import com.hypersocket.message.events.MessageResourceUpdatedEvent;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.MediaNotFoundException;
import com.hypersocket.realm.MediaType;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.replace.ReplacementUtils;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.triggers.ValidationException;
import com.hypersocket.upload.FileUpload;
import com.hypersocket.upload.FileUploadService;
import com.hypersocket.utils.ITokenResolver;


@Service
public class MessageResourceServiceImpl extends
		AbstractResourceServiceImpl<MessageResource> implements
		MessageResourceService {

	static Logger log = LoggerFactory.getLogger(MessageResourceServiceImpl.class);
	
	public static final String RESOURCE_BUNDLE = "MessageResourceService";

	@Autowired
	MessageResourceRepository repository;

	@Autowired
	I18NService i18nService;

	@Autowired
	PermissionService permissionService;

	@Autowired
	EventService eventService;
	
	@Autowired
	EmailNotificationService emailService;

	@Autowired
	RealmService realmService; 
	
	@Autowired
	FileUploadService uploadService; 
	
	@Autowired
	ConfigurationService configurationService;
	
	public MessageResourceServiceImpl() {
		super("Message");
	}
	
	@PostConstruct
	private void postConstruct() {

		i18nService.registerBundle(RESOURCE_BUNDLE);

		PermissionCategory cat = permissionService.registerPermissionCategory(
				RESOURCE_BUNDLE, "category.messages");

		for (MessageResourcePermission p : MessageResourcePermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		repository.loadPropertyTemplates("messageResourceTemplate.xml");

		eventService.registerEvent(
				MessageResourceEvent.class, RESOURCE_BUNDLE,
				this);
		eventService.registerEvent(
				MessageResourceCreatedEvent.class, RESOURCE_BUNDLE,
				this);
		eventService.registerEvent(
				MessageResourceUpdatedEvent.class, RESOURCE_BUNDLE,
				this);
		eventService.registerEvent(
				MessageResourceDeletedEvent.class, RESOURCE_BUNDLE,
				this);

		EntityResourcePropertyStore.registerResourceService(MessageResource.class, repository);
	}

	@Override
	protected AbstractResourceRepository<MessageResource> getRepository() {
		return repository;
	}

	@Override
	protected String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public Class<MessageResourcePermission> getPermissionType() {
		return MessageResourcePermission.class;
	}
	
	protected Class<MessageResource> getResourceClass() {
		return MessageResource.class;
	}
	
	@Override
	protected void fireResourceCreationEvent(MessageResource resource) {
		eventService.publishEvent(new MessageResourceCreatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceCreationEvent(MessageResource resource,
			Throwable t) {
		eventService.publishEvent(new MessageResourceCreatedEvent(this,
				resource, t, getCurrentSession()));
	}

	@Override
	protected void fireResourceUpdateEvent(MessageResource resource) {
		eventService.publishEvent(new MessageResourceUpdatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceUpdateEvent(MessageResource resource,
			Throwable t) {
		eventService.publishEvent(new MessageResourceUpdatedEvent(this,
				resource, t, getCurrentSession()));
	}

	@Override
	protected void fireResourceDeletionEvent(MessageResource resource) {
		eventService.publishEvent(new MessageResourceDeletedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceDeletionEvent(MessageResource resource,
			Throwable t) {
		eventService.publishEvent(new MessageResourceDeletedEvent(this,
				resource, t, getCurrentSession()));
	}

	@Override
	public MessageResource updateResource(MessageResource resource,
			String name, Map<String, String> properties)
			throws ResourceChangeException, AccessDeniedException {

		resource.setName(name);

		/**
		 * Set any additional fields on your resource here before calling
		 * updateResource.
		 * 
		 * Remember to fill in the fire*Event methods to ensure events are fired
		 * for all operations.
		 */
		updateResource(resource, properties);

		return resource;
	}

	@Override
	public MessageResource createResource(Integer messageId, String name, String subject, String body, Realm realm) throws ResourceCreationException,
			AccessDeniedException {
		return createResource(messageId, name, subject, body, "", true, false, null, realm);
	}
	
	@Override
	public MessageResource createI18nMessage(Integer messageId, String resourceBundle, String resourceKey, Realm realm) throws ResourceCreationException,
			AccessDeniedException {
		return createResource(messageId, I18N.getResource(Locale.getDefault(), resourceBundle, resourceKey + ".name"),
				I18N.getResource(Locale.getDefault(), resourceBundle, resourceKey + ".subject"), 
				I18N.getResource(Locale.getDefault(), resourceBundle, resourceKey + ".body"), 
				"", true, false, null, realm);
	}
	
	@Override
	public MessageResource createResource(Integer messageId, String name, String subject, String body, String html, 
			Boolean enabled, Boolean track, Realm realm) throws ResourceCreationException,
			AccessDeniedException {
		return createResource(messageId, name, subject, body, html, enabled, track, null, realm);
	}
	
	@Override
	public MessageResource createResource(Integer messageId, String name, String subject, String body, String html, 
			Boolean enabled, Boolean track, 
			Collection<FileUpload> attachments, Realm realm) throws ResourceCreationException,
			AccessDeniedException {

		MessageResource resource = new MessageResource();
		resource.setMessageId(messageId);
		resource.setSystem(true);
		resource.setName(name);
		resource.setRealm(realm);
		resource.setSubject(subject);
		resource.setBody(body);
		resource.setHtml(html);
		resource.setEnabled(enabled);
		resource.setTrack(track);
		
		List<String> attachmentUUIDs = new ArrayList<String>();
		if(attachments!=null) {
			for(FileUpload u : attachments) {
				attachmentUUIDs.add(u.getName());
			}
		}

		resource.setAttachments(ResourceUtils.implodeValues(attachmentUUIDs));
	
		createResource(resource,(Map<String,String>) null);

		return resource;
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate()
			throws AccessDeniedException {

		assertPermission(MessageResourcePermission.READ);

		return repository.getPropertyCategories(null);
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate(
			MessageResource resource) throws AccessDeniedException {

		assertPermission(MessageResourcePermission.READ);

		return repository.getPropertyCategories(resource);
	}

	@Override
	public MessageResource createResource(String name, Realm realm, Map<String, String> properties)
			throws ResourceCreationException, AccessDeniedException {
		
		MessageResource resource = new MessageResource();
		resource.setName(name);
		resource.setRealm(realm);
	
		createResource(resource, properties);

		return resource;
	}

	@Override
	public void sendMessage(Integer messageId, Realm realm, ITokenResolver tokenResolver, Principal... principals) throws ResourceNotFoundException, AccessDeniedException {
		
		MessageResource message = repository.getMessageById(messageId, realm);
		
		if(message==null) {
			throw new ResourceNotFoundException(RESOURCE_BUNDLE, "error.invalidMessageId", messageId);
		}
		
		if(!message.getEnabled()) {
			log.info(String.format("Message template %s has been disabled", message.getName()));
			return;
		}
		
		List<RecipientHolder> recipients = new ArrayList<RecipientHolder>();
		for(Principal principal : principals) {
			try {
				recipients.add(new RecipientHolder(new Recipient(principal.getPrincipalDescription(), 
						realmService.getPrincipalAddress(principal, MediaType.EMAIL), RecipientType.TO), principal));
			} catch (MediaNotFoundException e) {
				
			}
		}
		
		List<EmailAttachment> attachments = new ArrayList<EmailAttachment>();
		for(String uuid : ResourceUtils.explodeValues(message.getAttachments())) {
			try {
				FileUpload upload = uploadService.getFileUpload(uuid);
				attachments.add(new EmailAttachment(upload.getFileName(), 
						uploadService.getContentType(uuid), 
						uploadService.getInputStream(uuid)));
			} catch (IOException e) {
				
			}
		}
		if(!recipients.isEmpty()) {
			try {
		
				emailService.sendEmail(
						realm,
						ReplacementUtils.processTokenReplacements(message.getSubject(), tokenResolver),
						ReplacementUtils.processTokenReplacements(message.getBody(), tokenResolver),
						ReplacementUtils.processTokenReplacements(message.getHtml(), tokenResolver),
						message.getReplyToName(),
						message.getReplyToEmail(),
						recipients.toArray(new RecipientHolder[0]), 
						ResourceUtils.explodeValues(message.getAdditionalTo()),
						message.getTrack(), 
						configurationService.getIntValue(realm, "smtp.delay"),
						attachments.toArray(new EmailAttachment[0]));
				
			} catch (MailException | ValidationException e) {
				log.error("Failed to send email", e);
			}
		}

	}

}
