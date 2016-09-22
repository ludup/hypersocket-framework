package com.hypersocket.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import com.hypersocket.events.EventService;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.message.events.MessageResourceCreatedEvent;
import com.hypersocket.message.events.MessageResourceDeletedEvent;
import com.hypersocket.message.events.MessageResourceEvent;
import com.hypersocket.message.events.MessageResourceUpdatedEvent;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.tasks.email.EmailTask;
import com.hypersocket.upload.FileUpload;

@Service
public class MessageResourceServiceImpl extends
		AbstractResourceServiceImpl<MessageResource> implements
		MessageResourceService, ApplicationListener<SystemEvent> {

	public static final String RESOURCE_BUNDLE = "MessageResourceService";

	@Autowired
	MessageResourceRepository repository;

	@Autowired
	I18NService i18nService;

	@Autowired
	PermissionService permissionService;

	@Autowired
	EventService eventService;

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

		repository.getEntityStore().registerResourceService(MessageResource.class, repository);
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
	public MessageResource createResource(String name, String subject, String body, String html, 
			Boolean enabled, Boolean track, Class<? extends SystemEvent> fireEvent, 
			Collection<FileUpload> attachments, Realm realm,
			Map<String, String> properties) throws ResourceCreationException,
			AccessDeniedException {

		MessageResource resource = new MessageResource();
		resource.setSystem(true);
		resource.setName(name);
		resource.setRealm(realm);
		resource.setSubject(subject);
		resource.setBody(body);
		resource.setHtml(html);
		resource.setEnabled(enabled);
		resource.setTrack(track);
		resource.setEvent(fireEvent.getCanonicalName());
		
		List<String> attachmentUUIDs = new ArrayList<String>();
		for(FileUpload u : attachments) {
			attachmentUUIDs.add(u.getName());
		}
		resource.setAttachments(ResourceUtils.implodeValues(attachmentUUIDs));
	
		createResource(resource, properties);

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
	public void onApplicationEvent(SystemEvent event) {
		
		if(event instanceof MessageEvent) {
			
			// Lookup message template
			Collection<MessageResource> messages = repository.getMessagesByEvent(event.getClass().getCanonicalName());
			for(MessageResource message : messages) {
				
			}
		}
	}

}
