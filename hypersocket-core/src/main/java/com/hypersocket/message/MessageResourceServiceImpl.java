package com.hypersocket.message;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.email.EmailBatchService;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18N;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.message.events.MessageResourceCreatedEvent;
import com.hypersocket.message.events.MessageResourceDeletedEvent;
import com.hypersocket.message.events.MessageResourceEvent;
import com.hypersocket.message.events.MessageResourceUpdatedEvent;
import com.hypersocket.messagedelivery.MessageDeliveryService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.PrincipalWithoutPasswordResolver;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmAdapter;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.UserPrincipal;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.resource.TransactionAdapter;
import com.hypersocket.upload.FileUpload;
import com.hypersocket.upload.FileUploadService;

@Service
public class MessageResourceServiceImpl extends AbstractResourceServiceImpl<MessageResource>
		implements MessageResourceService {

	static Logger log = LoggerFactory.getLogger(MessageResourceServiceImpl.class);

	public static final String RESOURCE_BUNDLE = "MessageResourceService";

	@Autowired
	private MessageResourceRepository repository;

	@Autowired
	private I18NService i18nService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private EventService eventService;

	@Autowired
	private RealmService realmService;

	@Autowired
	private FreeMarkerService templateService;

	@Autowired
	private EmailBatchService batchService;

	@Autowired
	private MessageDeliveryService messageDeliveryService;

	@Autowired
	private FileUploadService uploadService;

	private Map<String, MessageRegistration> messageRegistrations = new HashMap<String, MessageRegistration>();
	private List<String> messageIds = new ArrayList<String>();

	public MessageResourceServiceImpl() {
		super("Message");
	}

	@PostConstruct
	private void postConstruct() {

		i18nService.registerBundle(RESOURCE_BUNDLE);

		PermissionCategory cat = permissionService.registerPermissionCategory(RESOURCE_BUNDLE, "category.messages");

		for (MessageResourcePermission p : MessageResourcePermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		repository.loadPropertyTemplates("messageResourceTemplate.xml");

		eventService.registerEvent(MessageResourceEvent.class, RESOURCE_BUNDLE, this);
		eventService.registerEvent(MessageResourceCreatedEvent.class, RESOURCE_BUNDLE, this);
		eventService.registerEvent(MessageResourceUpdatedEvent.class, RESOURCE_BUNDLE, this);
		eventService.registerEvent(MessageResourceDeletedEvent.class, RESOURCE_BUNDLE, this);

		EntityResourcePropertyStore.registerResourceService(MessageResource.class, repository);

		realmService.registerRealmListener(new RealmAdapter() {

			@Override
			public void onCreateRealm(Realm realm) throws ResourceException, AccessDeniedException {

				if(realm.hasOwner()) {
					/**
					 * We don't want messages in secondary realms.
					 */
					return;
				}
				
				for (MessageRegistration r : messageRegistrations.values()) {
					try {
						MessageResource message = getMessageById(r.getMessageId(), realm);
						if (message == null) {
							message = repository.getResourceByName(
									I18N.getResource(Locale.getDefault(), r.resourceBundle, r.resourceKey + ".name"),
									realm);
						}
						String existingName = I18N.getResource(Locale.getDefault(), r.resourceBundle,
								r.resourceKey + ".name");
						if (message == null || !message.getName().equals(existingName)) {
							if (r.systemOnly && !realm.isSystem()) {
								continue;
							}
							if (!Objects.isNull(message)) {
								deleteResource(message);
							}
							createI18nMessage(r.resourceBundle, r.resourceKey, r.variables, realm, r.enabled, r.delivery);
							if (r.repository != null) {
								r.repository.onCreated(getMessageById(r.getMessageId(), realm));
							}
						} else {
							if (message.getResourceKey() == null) {
								message.setResourceKey(r.resourceKey);
								repository.saveResource(message);
							}
							String vars = ResourceUtils.implodeValues(r.variables);
							if (!vars.equals(message.getSupportedVariables())) {
								message.setSupportedVariables(vars);
								repository.saveResource(message);
							}
						}
					} catch (Exception e) {
						log.error("Faied to create message template", e);
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
		eventService.publishEvent(new MessageResourceCreatedEvent(this, getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceCreationEvent(MessageResource resource, Throwable t) {
		eventService.publishEvent(new MessageResourceCreatedEvent(this, resource, t, getCurrentSession()));
	}

	@Override
	protected void fireResourceUpdateEvent(MessageResource resource) {
		eventService.publishEvent(new MessageResourceUpdatedEvent(this, getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceUpdateEvent(MessageResource resource, Throwable t) {
		eventService.publishEvent(new MessageResourceUpdatedEvent(this, resource, t, getCurrentSession()));
	}

	@Override
	protected void fireResourceDeletionEvent(MessageResource resource) {
		eventService.publishEvent(new MessageResourceDeletedEvent(this, getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceDeletionEvent(MessageResource resource, Throwable t) {
		eventService.publishEvent(new MessageResourceDeletedEvent(this, resource, t, getCurrentSession()));
	}

	@Override
	public MessageResource updateResource(MessageResource resource, String name, Map<String, String> properties)
			throws ResourceException, AccessDeniedException {

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
		MessageRegistration r = messageRegistrations.get(resource.getResourceKey());
		if (r == null) {
			throw new IllegalStateException(String.format("Missing message template id %d", resource.getId()));
		}
		if (r.repository != null) {
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
	public void registerI18nMessage(String resourceBundle, String resourceKey, Set<String> variables) {
		registerI18nMessage(resourceBundle, resourceKey, variables, false);
	}

	@Override
	public void registerI18nMessage(String resourceBundle, String resourceKey, Set<String> variables, boolean system) {
		registerI18nMessage(resourceBundle, resourceKey, variables, system, null);
	}

	@Override
	public void registerI18nMessage(String resourceBundle, String resourceKey, Set<String> variables, boolean system,
			MessageTemplateRepository repository) {
		registerI18nMessage(resourceBundle, resourceKey, variables, system, repository, true,
				EmailDeliveryStrategy.PRIMARY);
	}

	@Override
	public void registerI18nMessage(String resourceBundle, String resourceKey, Set<String> variables, boolean system,
			MessageTemplateRepository repository, boolean enabled) {
		registerI18nMessage(resourceBundle, resourceKey, variables, system, repository, enabled,
				EmailDeliveryStrategy.PRIMARY);
	}

	@Override
	public void registerI18nMessage(String resourceBundle, String resourceKey, Set<String> variables, boolean system,
			MessageTemplateRepository repository, boolean enabled, EmailDeliveryStrategy delivery) {
		MessageRegistration r = new MessageRegistration();
		r.resourceBundle = resourceBundle;
		r.resourceKey = resourceKey;
		r.variables = variables;
		r.systemOnly = system;
		r.repository = repository;
		r.enabled = enabled;
		r.delivery = delivery;

		messageRegistrations.put(r.getMessageId(), r);
		messageIds.add(r.getMessageId());
	}

	private void createI18nMessage(String resourceBundle, String resourceKey, Set<String> variables, Realm realm,
			boolean enabled, EmailDeliveryStrategy delivery) throws ResourceException, AccessDeniedException {
		String plainBody = I18N.getResource(Locale.getDefault(), resourceBundle, resourceKey + ".body");
		String htmlBody = null;
		try {
			htmlBody = I18N.getResourceOrException(Locale.getDefault(), resourceBundle, resourceKey + ".html");
		} catch (MissingResourceException mre) {
		}
		createResource(resourceKey, I18N.getResource(Locale.getDefault(), resourceBundle, resourceKey + ".name"),
				I18N.getResource(Locale.getDefault(), resourceBundle, resourceKey + ".subject"), plainBody, htmlBody,
				variables, enabled, false, null, realm, delivery);
	}

	@Override
	public MessageResource createResource(String resourceKey, String name, String subject, String body, String html,
			Set<String> variables, Boolean enabled, Boolean track, Collection<FileUpload> attachments, Realm realm,
			EmailDeliveryStrategy delivery) throws ResourceException, AccessDeniedException {

		MessageResource resource = new MessageResource();
		resource.setResourceKey(resourceKey);
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
		if (attachments != null) {
			for (FileUpload u : attachments) {
				attachmentUUIDs.add(u.getName());
			}
		}

		resource.setAttachments(ResourceUtils.implodeValues(attachmentUUIDs));

		createResource(resource, (Map<String, String>) null);

		return resource;
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate() throws AccessDeniedException {

		assertPermission(MessageResourcePermission.READ);

		return repository.getPropertyCategories(null);
	}

	@Override
	public Collection<PropertyCategory> getPropertyTemplate(MessageResource resource) throws AccessDeniedException {

		assertPermission(MessageResourcePermission.READ);

		List<PropertyCategory> results = new ArrayList<PropertyCategory>(repository.getPropertyCategories(resource));

		MessageRegistration r = messageRegistrations.get(resource.getResourceKey());

		if (r == null) {
			throw new IllegalStateException(String.format("Missing message template id %s", resource.getResourceKey()));
		}

		if (r.repository != null) {
			results.addAll(r.repository.getPropertyCategories(resource));
		}

		return results;
	}

	@Override
	public MessageResource createResource(String name, Realm realm, Map<String, String> properties)
			throws ResourceException, AccessDeniedException {

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
		vars.addAll(Arrays.asList("trackingImage", "email", "firstName", "fullName", "principalId", "serverUrl",
				"serverName", "serverHost"));
		return vars;
	}
	
	@Override
	public MessageSender newMessageSender(Realm realm) {
		return new MessageSender(realm, realmService, templateService, batchService, messageDeliveryService, uploadService, this);
	}

	@Override
	@Transactional
	public void test(MessageResource message, String email) throws ResourceNotFoundException, AccessDeniedException {
		try {
			newMessageSender(message.getRealm()).messageResource(message).tokenResolver(new PrincipalWithoutPasswordResolver((UserPrincipal<?>)realmService.getPrincipalByEmail(message.getRealm(), email))).recipientAddress(email).ignoreDisabledFlag(true).sendOrError();
		} catch (Exception e) {
			log.error("Failed to send test.", e);
			throw new IllegalStateException("Failed to send test email. ", e);
		}
	}

	class MessageRegistration {
		Set<String> variables;
		String resourceBundle;
		String resourceKey;
		boolean systemOnly;
		MessageTemplateRepository repository;
		boolean enabled = true;
		EmailDeliveryStrategy delivery;

		public String getMessageId() {
			return resourceKey;
		}
	}

	@Override
	public MessageResource getMessageById(String resourceKey, Realm realm) {
		
		if(realm.hasOwner()) {
			Realm r = realm.getParent();
			if(Objects.nonNull(r)) {
				realm = r;
			}
		}
		
		return repository.getMessageById(resourceKey, realm);
	}


}
