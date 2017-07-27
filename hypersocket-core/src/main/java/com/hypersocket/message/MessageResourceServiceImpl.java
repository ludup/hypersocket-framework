package com.hypersocket.message;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.codemonkey.simplejavamail.MailException;
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
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmAdapter;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.ServerResolver;
import com.hypersocket.realm.UserPrincipal;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.resource.TransactionAdapter;
import com.hypersocket.triggers.ValidationException;
import com.hypersocket.upload.FileUpload;
import com.hypersocket.upload.FileUploadService;
import com.hypersocket.utils.ITokenResolver;

import freemarker.template.Template;
import freemarker.template.TemplateException;


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
	
	@Autowired
	FreeMarkerService templateService; 
	
	Map<Integer,MessageRegistration> messageRegistrations = new HashMap<Integer, MessageRegistration>();
	List<Integer> messageIds = new ArrayList<Integer>();
	
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
		
		realmService.registerRealmListener(new RealmAdapter() {
			
			@Override
			public void onCreateRealm(Realm realm) throws ResourceException, AccessDeniedException {
				
				for(MessageRegistration r : messageRegistrations.values()) {
					MessageResource message = repository.getMessageById(r.messageId, realm);
					if(message==null) {
						if(r.systemOnly && !realm.isSystem()) {
							continue;
						}
						createI18nMessage(r.messageId, r.resourceBundle, r.resourceKey, r.variables, realm, r.enabled, r.delivery);
						if(r.repository!=null) {
							r.repository.onCreated(getMessageById(r.messageId, realm));
						}
					} else {
						String vars = ResourceUtils.implodeValues(r.variables);
						if(!vars.equals(message.getSupportedVariables())) {
							message.setSupportedVariables(vars);
							repository.saveResource(message);
						}
					}
				}
			}
			
			@Override
			public boolean hasCreatedDefaultResources(Realm realm) {
				return false;
			}
		});
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
		updateResource(resource, properties, new TransactionAdapter<MessageResource>() {

			@Override
			public void afterOperation(MessageResource resource, Map<String, String> properties) {
				setProperties(resource, properties);
			}
		});

		return resource;
	}

	private void setProperties(MessageResource resource, Map<String, String> properties) {
		MessageRegistration r = messageRegistrations.get(resource.getMessageId());
		if(r==null) {
			throw new IllegalStateException(String.format("Missing message template id %d", resource.getId()));
		}
		if(r.repository!=null) {
			MessageTemplateRepository repository = r.repository;
			for (String resourceKey : repository.getPropertyNames(resource)) {
				if (properties.containsKey(resourceKey)) {
					repository.setValue(resource, resourceKey, properties.get(resourceKey));
				}
			}
			repository.onUpdated(resource);
		}
	}
	
	@Override
	public void registerI18nMessage(Integer messageId, String resourceBundle, 
			String resourceKey, Set<String> variables) {
		registerI18nMessage(messageId, resourceBundle, resourceKey, variables, false);
	}
	
	@Override
	public void registerI18nMessage(Integer messageId, String resourceBundle, 
			String resourceKey, Set<String> variables, boolean system) {
		registerI18nMessage(messageId, resourceBundle, resourceKey, variables, system, null);
	}
	
	@Override
	public void registerI18nMessage(Integer messageId, String resourceBundle, 
			String resourceKey, Set<String> variables, boolean system,
			MessageTemplateRepository repository) {
		registerI18nMessage(messageId, resourceBundle, resourceKey, variables, system, repository, true, EmailDeliveryStrategy.PRIMARY);
	}
	
	@Override
	public void registerI18nMessage(Integer messageId, String resourceBundle, 
			String resourceKey, Set<String> variables, boolean system,
			MessageTemplateRepository repository, boolean enabled) {
		registerI18nMessage(messageId, resourceBundle, resourceKey, variables, system, repository, enabled, EmailDeliveryStrategy.PRIMARY);
	}
	@Override
	public void registerI18nMessage(Integer messageId, String resourceBundle, 
			String resourceKey, Set<String> variables, boolean system,
			MessageTemplateRepository repository, boolean enabled, EmailDeliveryStrategy delivery) {
		MessageRegistration r = new MessageRegistration();
		r.messageId = messageId;
		r.resourceBundle = resourceBundle;
		r.resourceKey = resourceKey;
		r.variables = variables;
		r.systemOnly = system;
		r.repository = repository;
		r.enabled = enabled;
		r.delivery = delivery;
		
		messageRegistrations.put(messageId, r);
		messageIds.add(messageId);
	}
	
	private void createI18nMessage(Integer messageId, String resourceBundle, 
			String resourceKey, Set<String> variables, Realm realm, boolean enabled, EmailDeliveryStrategy delivery) throws ResourceCreationException,
			AccessDeniedException {
		createResource(messageId, I18N.getResource(Locale.getDefault(), resourceBundle, resourceKey + ".name"),
				I18N.getResource(Locale.getDefault(), resourceBundle, resourceKey + ".subject"), 
				I18N.getResource(Locale.getDefault(), resourceBundle, resourceKey + ".body"), 
				"", variables, enabled, false, null, realm, delivery);
	}

	@Override
	public MessageResource createResource(Integer messageId, String name, String subject, String body, String html, 
			Set<String> variables,
			Boolean enabled, Boolean track, 
			Collection<FileUpload> attachments, Realm realm, EmailDeliveryStrategy delivery) throws ResourceCreationException,
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
		resource.setDeliveryStrategy(delivery);
		resource.setSupportedVariables(ResourceUtils.implodeValues(variables));
		
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

		List<PropertyCategory> results = new ArrayList<PropertyCategory>(repository.getPropertyCategories(resource));
		
		MessageRegistration r = messageRegistrations.get(resource.getMessageId());
		
		if(r==null) {
			throw new IllegalStateException(String.format("Missing message template id %d", resource.getMessageId()));
		}
		
		if(r.repository!=null) {
			results.addAll(r.repository.getPropertyCategories(resource));
		}
		
		return results;
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
	public Set<String> getMessageVariables(MessageResource message) {
		
		Set<String> vars = new TreeSet<String>();
		vars.addAll(ResourceUtils.explodeCollectionValues(message.getSupportedVariables()));
		vars.addAll(Arrays.asList("trackingImage", "email", "firstName",
				"fullName", "principalId", "serverUrl", "serverName", "serverHost"));
		return vars;
	}

	@Override
	public void sendMessage(Integer messageId, Realm realm, ITokenResolver tokenResolver, Principal... principals) {
		sendMessage(messageId, realm, tokenResolver, Arrays.asList(principals));
	}
	
	@Override
	public void sendMessageToEmailAddress(Integer messageId, Realm realm, ITokenResolver tokenResolver, String... principals) {
		sendMessageToEmailAddress(messageId, realm, tokenResolver, Arrays.asList(principals));
	}
	
	@Override
	public void sendMessageToEmailAddress(Integer messageId, Realm realm, ITokenResolver tokenResolver, Collection<String> emails) {
	
		MessageResource message = repository.getMessageById(messageId, realm);
		
		if(message==null) {
			log.error(String.format("Invalid message id %d", messageId));
			return;
		}
		
		List<RecipientHolder> recipients = new ArrayList<RecipientHolder>();
		for(String email : emails) {
			recipients.add(new RecipientHolder(ResourceUtils.getNamePairKey(email),
					ResourceUtils.getNamePairValue(email)));
		}
		
		
		sendMessage(message, realm, tokenResolver, recipients);
	}
	
	@Override
	public void sendMessage(Integer messageId, Realm realm, ITokenResolver tokenResolver, Collection<Principal> principals) {

		MessageResource message = repository.getMessageById(messageId, realm);
		
		if(message==null) {
			log.error(String.format("Invalid message id %d", messageId));
			return;
		}
		
		List<RecipientHolder> recipients = new ArrayList<RecipientHolder>();
		EmailDeliveryStrategy strategy = message.getDeliveryStrategy();
		
		if(strategy!=EmailDeliveryStrategy.ONLY_ADDITIONAL) {
			for(Principal principal : principals) {
				switch(strategy) {
				case ALL:
					recipients.add(new RecipientHolder(principal, 
							principal.getEmail()));
					for(String email : ResourceUtils.explodeCollectionValues(((UserPrincipal)principal).getSecondaryEmail())) {
						recipients.add(new RecipientHolder(principal, email));
					}
					break;
				case PRIMARY:
					recipients.add(new RecipientHolder(principal, 
							principal.getEmail()));
					break;
				case SECONDARY:
					for(String email : ResourceUtils.explodeCollectionValues(((UserPrincipal)principal).getSecondaryEmail())) {
						recipients.add(new RecipientHolder(principal, email));
					}
					break;
				default:
				}
			}
		}
		
		sendMessage(message, realm, tokenResolver, recipients);
	}
	
	
	private void sendMessage(MessageResource message, Realm realm, ITokenResolver tokenResolver, List<RecipientHolder> recipients ) {
		
		if(!message.getEnabled()) {
			log.info(String.format("Message template %s has been disabled", message.getName()));
			return;
		}
		
		List<EmailAttachment> attachments = new ArrayList<EmailAttachment>();
		for(String uuid : ResourceUtils.explodeValues(message.getAttachments())) {
			try {
				FileUpload upload = uploadService.getFileUpload(uuid);
				attachments.add(new EmailAttachment(upload.getFileName(), 
						uploadService.getContentType(uuid), 
						uploadService.getInputStream(uuid)));
			} catch (ResourceNotFoundException | IOException e) {
				log.error(String.format("Unable to locate upload %s", uuid), e);
			}
		}
		
		for(String additional : ResourceUtils.explodeCollectionValues(message.getAdditionalTo())) {
			recipients.add(new RecipientHolder("", additional));
		}
		
		try {
	
			Map<String,Object> data = tokenResolver.getData();
			data.putAll(new ServerResolver(realm).getData());
			
			Template subjectTemplate = templateService.createTemplate("message.subject." + message.getId(), 
					message.getSubject(), 
					message.getModifiedDate().getTime());
			StringWriter subjectWriter = new StringWriter();
			subjectTemplate.process(data, subjectWriter);
			
			Template bodyTemplate = templateService.createTemplate("message.body." + message.getId(), 
					message.getBody(), 
					message.getModifiedDate().getTime());
			StringWriter bodyWriter = new StringWriter();
			bodyTemplate.process(data, bodyWriter);
			
			Template htmlTemplate = templateService.createTemplate("message.html." + message.getId(), 
					message.getHtml(), 
					message.getModifiedDate().getTime());				
			StringWriter htmlWriter = new StringWriter();
			htmlTemplate.process(data, htmlWriter);
			
			emailService.sendEmail(
					realm,
					subjectWriter.toString(),
					bodyWriter.toString(),
					htmlWriter.toString(),
					message.getReplyToName(),
					message.getReplyToEmail(),
					recipients.toArray(new RecipientHolder[0]), 
					null,
					message.getTrack(), 
					configurationService.getIntValue(realm, "smtp.delay"),
					attachments.toArray(new EmailAttachment[0]));
			
		} catch (MailException e) { 
			// Will be logged by mail API
		} catch(AccessDeniedException | IOException | TemplateException | ValidationException e) {
			log.error("Failed to send email", e);
		}
		
	}
	
	class MessageRegistration {
		Set<String> variables;
		Integer messageId;
		String resourceBundle;
		String resourceKey;
		boolean systemOnly;
		MessageTemplateRepository repository;
		boolean enabled = true;
		EmailDeliveryStrategy delivery;
	}

	@Override
	public MessageResource getMessageById(Integer id, Realm realm) {
		return repository.getMessageById(id, realm);
	}

}
