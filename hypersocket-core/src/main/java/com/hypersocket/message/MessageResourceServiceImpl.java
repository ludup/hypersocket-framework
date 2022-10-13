package com.hypersocket.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.simplejavamail.MailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.email.EmailAttachment;
import com.hypersocket.email.EmailBatchService;
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
import com.hypersocket.realm.PrincipalWithoutPasswordResolver;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmAdapter;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.ServerResolver;
import com.hypersocket.realm.UserPrincipal;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.resource.TransactionAdapter;
import com.hypersocket.triggers.ValidationException;
import com.hypersocket.upload.FileUpload;
import com.hypersocket.upload.FileUploadService;
import com.hypersocket.util.CompoundIterator;
import com.hypersocket.util.ProxiedIterator;
import com.hypersocket.util.TransformingIterator;
import com.hypersocket.utils.ITokenResolver;

import freemarker.template.Template;
import freemarker.template.TemplateException;

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
	private EmailNotificationService emailService;

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
	@Transactional
	public void test(MessageResource message, String email) throws ResourceNotFoundException, AccessDeniedException {
		UserPrincipal<?> user = (UserPrincipal<?>) realmService.getPrincipalByEmail(message.getRealm(), email);
		Collection<String> recipients = StringUtils.isEmpty(email) ? null : Arrays.asList(email);
		sendMessage(message, user.getRealm(), new PrincipalWithoutPasswordResolver(user), null,
				(Iterator<Principal>)null, recipients, null,
						null, (String)null);
	}

	@Override
	public void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver, Principal... principals)
			throws ResourceException {
		sendMessage(resourceKey, realm, tokenResolver, Arrays.asList(principals));
	}

	@Override
	public void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver, RecipientHolder replyTo,
			List<EmailAttachment> attachments, Iterator<Principal> principals, String context) {
		sendMessage(resourceKey, realm, tokenResolver, replyTo, attachments, principals,
				Collections.<String>emptyList(), null);
	}
	
	@Override
	public void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver, RecipientHolder recipient) {
		sendMessage(resourceKey, realm, tokenResolver, recipient, Collections.emptyList());
	}

	@Override
	public void sendMessageToEmailAddress(String resourceKey, Realm realm, ITokenResolver tokenResolver,
			String... principals) {
		sendMessageToEmailAddress(resourceKey, realm, tokenResolver, Arrays.asList(principals), null, null);
	}

	@Override
	public void sendMessageToEmailAddress(String resourceKey, Realm realm, ITokenResolver tokenResolver,
			Collection<String> emails, List<EmailAttachment> attachments, String context) {

		MessageResource message = getMessageById(resourceKey, realm);

		if (message == null) {
			log.error(String.format("Invalid message id %s", resourceKey));
			return;
		}

		sendMessage(message, realm, tokenResolver, null,
				new TransformingIterator<String, RecipientHolder>(emails.iterator()) {
					@Override
					protected RecipientHolder transform(String email) {
						return new RecipientHolder(ResourceUtils.getNamePairKey(email),
								ResourceUtils.getNamePairValue(email));
					}
				}, attachments, context);
	}

	@Override
	public void sendMessageToEmailAddress(String resourceKey, Realm realm, Collection<RecipientHolder> recipients,
			RecipientHolder replyTo, ITokenResolver tokenResolver, List<EmailAttachment> attachments, String context) {
		MessageResource message = getMessageById(resourceKey, realm);

		if (message == null) {
			log.error(String.format("Invalid message id %s", resourceKey));
			return;
		}

		sendMessage(message, realm, tokenResolver, replyTo, recipients.iterator(), attachments, context);

	}

	@Override
	public void sendMessageToEmailAddress(String resourceKey, Realm realm, Collection<RecipientHolder> recipients,
			ITokenResolver tokenResolver) {
		sendMessageToEmailAddress(resourceKey, realm, recipients, null, tokenResolver, null, null);
	}

	@Override
	public void sendMessageNow(String resourceKey, Realm realm, ITokenResolver tokenResolver,
			Collection<Principal> principals) {
		sendMessage(resourceKey, realm, tokenResolver, principals.iterator(), Collections.<String>emptyList(), null);
	}

	@Override
	public void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver, RecipientHolder replyTo,
			List<EmailAttachment> attachments, Iterator<Principal> principals, Collection<String> emails, String context) {
		sendMessage(resourceKey, realm, tokenResolver, replyTo, principals, emails, new Date(), attachments);
	}

	@Override
	public void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver,
			Collection<Principal> principals) throws ResourceException {
		sendMessage(resourceKey, realm, tokenResolver, principals.iterator(), Collections.<String>emptyList(),
				new Date());
	}

	@Override
	public void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver,
			Iterator<Principal> principals) throws ResourceException {
		sendMessage(resourceKey, realm, tokenResolver, principals, Collections.<String>emptyList(), new Date());
	}

	@Override
	public void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver,
			Iterator<Principal> principals, Collection<String> emails, Date schedule) {
		sendMessage(resourceKey, realm, tokenResolver, null, principals, emails, schedule, null);
	}

	@Override
	public void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver, RecipientHolder replyTo,
			Iterator<Principal> principals, Collection<String> emails, Date schedule,
			List<EmailAttachment> attachments) {
		
		MessageResource message = getMessageById(resourceKey, realm);

		if (message == null) {
			log.error(String.format("Invalid message id %s", resourceKey));
			return;
		}

		sendMessage(message, message.getRealm(), tokenResolver, replyTo, principals, emails, schedule, attachments, null);
	}
	
	@Override
	public void sendMessage(String resourceKey, Realm realm, 
			ITokenResolver tokenResolver, RecipientHolder replyTo,
			Collection<Principal> principals) {
		
		MessageResource message = getMessageById(resourceKey, realm);

		if (message == null) {
			log.error(String.format("Invalid message id %s", resourceKey));
			return;
		}
		
		sendMessage(message, realm, tokenResolver, replyTo, 
				principals.iterator(), Collections.<String>emptyList(), null, 
				Collections.<EmailAttachment>emptyList(), null);
	}
	
	@Override
	public void sendMessage(MessageResource message, Realm realm, ITokenResolver tokenResolver, RecipientHolder replyTo,
			Iterator<Principal> principals, Collection<String> emails, Date schedule,
			List<EmailAttachment> attachments, String context) {

		EmailDeliveryStrategy strategy = message.getDeliveryStrategy();

		List<RecipientHolder> validated = new ArrayList<>();
		if (emails != null && !emails.isEmpty()) {
			/**
			 * LDP discovered issue where a blank value was in this collection. 
			 * Guard against that here.
			 */
			for(String email : emails) {
				if(StringUtils.isNotBlank(email)) {
					validated.add(new RecipientHolder(email.toLowerCase()));
				}
			}
		}
		
		CompoundIterator<RecipientHolder> recipients = new CompoundIterator<>();

		if (strategy != EmailDeliveryStrategy.ONLY_ADDITIONAL) {
			recipients.addIterator(new ProxiedIterator<RecipientHolder>() {

				Iterator<String> childAddressIt;
				Principal principal;

				@Override
				protected RecipientHolder checkNext(RecipientHolder item) {
					if (item == null) {
						while (item == null) {
							if (childAddressIt != null) {
								if (childAddressIt.hasNext()) {
									item = new RecipientHolder(principal, childAddressIt.next());
								} else
									childAddressIt = null;
							}
							if (item == null) {
								if (principals!=null && principals.hasNext()) {
									principal = principals.next();
									if(!realmService.isDisabled(principal) && !realmService.getUserPropertyBoolean(principal, "user.bannedEmail")) {
										switch (strategy) {
										case ALL:
											item = new RecipientHolder(principal, principal.getEmail());
											Iterator<String> it = ResourceUtils.explodeCollectionValues(
													((UserPrincipal<?>) principal).getSecondaryEmail()).iterator();
											if (it.hasNext())
												childAddressIt = it;
											break;
										case PRIMARY:
											item = new RecipientHolder(principal, principal.getEmail());
											break;
										case SECONDARY:
											it = ResourceUtils.explodeCollectionValues(
													((UserPrincipal<?>) principal).getSecondaryEmail()).iterator();
											if (it.hasNext())
												childAddressIt = it;
											break;

										default:
										}
									} else {
										log.info("{} is has an email ban", principal.getPrincipalName());
									}
								}
								else
									break;
							}
						}
					}
					return item;
				}
			});
		}
	
		recipients.addIterator(validated.iterator());

		sendMessage(message, realm, tokenResolver, replyTo, recipients, schedule, null, context);
	}

	private void sendMessage(MessageResource message, Realm realm, ITokenResolver tokenResolver,
			RecipientHolder replyTo, Iterator<RecipientHolder> recipients, List<EmailAttachment> attachments, String context) {
		sendMessage(message, realm, tokenResolver, replyTo, recipients, new Date(), attachments, context);
	}

	private void sendMessage(MessageResource message, Realm realm, ITokenResolver tokenResolver,
			RecipientHolder replyTo, Iterator<RecipientHolder> recipients, Date schedule,
			List<EmailAttachment> attachments, String context) {

		if (!message.getEnabled()) {
			log.info(String.format("Message template %s has been disabled", message.getName()));
			return;
		}

		final List<String> additionalRecipients = ResourceUtils.explodeCollectionValues(message.getAdditionalTo());
		if (!additionalRecipients.isEmpty()) {
			CompoundIterator<RecipientHolder> cit = new CompoundIterator<RecipientHolder>();
			cit.addIterator(recipients);
			cit.addIterator(new TransformingIterator<String, RecipientHolder>(additionalRecipients.iterator()) {
				@Override
				protected RecipientHolder transform(String from) {
					return new RecipientHolder("", from);
				}
			});
			recipients = cit;
		}

		/**
		 * LDP - Final guard against a user receiving duplicate messages 
		 */
		Set<String> processedEmails = new HashSet<>();
		
		try {
			while (recipients.hasNext()) {
				RecipientHolder recipient = recipients.next();

				if(StringUtils.isBlank(recipient.getEmail())) {
					log.warn("Detected empty email in a RecipientHolder! Skipping");
					continue;
				}
				
				if(processedEmails.contains(recipient.getEmail().toLowerCase())) {
					log.info("Skipping {} because we already sent this message to that address", recipient.getEmail());
					continue;
				}
				
				processedEmails.add(recipient.getEmail().toLowerCase());
				
				Map<String, Object> data = tokenResolver.getData();
				data.putAll(new ServerResolver(realm).getData());
				data.put("email", recipient.getEmail());
				data.put("firstName", recipient.getFirstName());
				data.put("fullName", recipient.getName());
				data.put("principalId", recipient.getPrincipalId());

				if(recipient.hasPrincipal()) {
					
					if(realmService.getUserPropertyBoolean(recipient.getPrincipal(), "user.bannedEmail")) {
						log.warn("User {} has email ban turned on. Ignoring", recipient.getPrincipal().getName());
						continue;
					}
					/* 
					 * #V1HT82 - Issue Title is not showing in outgoing email
					 * 
					 * Don't overwrite variables provided by the token resolver, 
					 * they should have higher priority.
					 */
					final Map<String, String> userProps = realmService.getUserPropertyValues(recipient.getPrincipal());
					for(Map.Entry<String, String> en : userProps.entrySet()) {
						if(!data.containsKey(en.getKey()))
							data.put(en.getKey(), en.getValue());
					}
				}
				
				Template subjectTemplate = templateService.createTemplate("message.subject." + message.getId(),
						message.getSubject(), message.getModifiedDate().getTime());
				StringWriter subjectWriter = new StringWriter();
				subjectTemplate.process(data, subjectWriter);

				Template bodyTemplate = templateService.createTemplate("message.body." + message.getId(),
						message.getBody(), message.getModifiedDate().getTime());
				StringWriter bodyWriter = new StringWriter();
				bodyTemplate.process(data, bodyWriter);

				String receipientHtml = "";

				if (StringUtils.isNotBlank(message.getHtml())) {
					if (message.getHtmlTemplate() != null) {
						Document doc = Jsoup.parse(message.getHtmlTemplate().getHtml());
						Elements elements = doc.select(message.getHtmlTemplate().getContentSelector());
						if (elements.isEmpty()) {
							throw new IllegalStateException(String.format("Invalid content selector %s",
									message.getHtmlTemplate().getContentSelector()));
						}
						elements.first().append(message.getHtml());
						receipientHtml = doc.toString();
					} else {
						receipientHtml = message.getHtml();
					}
				}

				Template htmlTemplate = templateService.createTemplate("message.html." + message.getId(),
						receipientHtml, message.getModifiedDate().getTime());

				data.put("htmlTitle", subjectWriter.toString());

				StringWriter htmlWriter = new StringWriter();
				htmlTemplate.process(data, htmlWriter);

				String attachmentsListString = message.getAttachments();
				List<String> attachmentUUIDs = new ArrayList<>(
						Arrays.asList(ResourceUtils.explodeValues(attachmentsListString)));

				if (tokenResolver instanceof ResolverWithAttachments) {
					attachmentUUIDs.addAll(((ResolverWithAttachments) tokenResolver).getAttachmentUUIDS());
				}

				if (attachments != null) {
					for (EmailAttachment attachment : attachments) {
						attachmentUUIDs.add(attachment.getName());
					}
				}

				if(Boolean.getBoolean("hypersocket.disableBatchEmails") || Objects.isNull(schedule)) {
					
					List<EmailAttachment> emailAttachments = new ArrayList<EmailAttachment>();
					if (attachments != null) {
						emailAttachments.addAll(attachments);
					}
					for (String uuid : attachmentUUIDs) {
						try {
							FileUpload upload = uploadService.getFileUpload(uuid);
							emailAttachments
									.add(new EmailAttachment(upload.getFileName(), uploadService.getContentType(uuid)) {
										@Override
										public InputStream getInputStream() throws IOException {
											return uploadService.getInputStream(getName());
										}
									});
						} catch (ResourceNotFoundException | IOException e) {
							log.error(String.format("Unable to locate upload %s", uuid), e);
						}
					}

					emailService.sendEmail(realm, subjectWriter.toString(), bodyWriter.toString(),
							htmlWriter.toString(), 
							replyTo != null ? replyTo.getName() : message.getReplyToName(),
							replyTo != null ? replyTo.getEmail() : message.getReplyToEmail(),
							new RecipientHolder[] { recipient }, message.getArchive(), message.getTrack(), 50,
							context, emailAttachments.toArray(new EmailAttachment[0]));
				} else {
					attachmentsListString = ResourceUtils.implodeValues(attachmentUUIDs);

					batchService.scheduleEmail(realm, subjectWriter.toString(), bodyWriter.toString(),
							htmlWriter.toString(), replyTo != null ? replyTo.getName() : message.getReplyToName(),
							replyTo != null ? replyTo.getEmail() : message.getReplyToEmail(), recipient.getName(),
							recipient.getEmail(), message.getArchive(), message.getTrack(), attachmentsListString, schedule, context);

				}
			}

		} catch (MailException e) {
			// Will be logged by mail API
		} catch (AccessDeniedException | ValidationException | IOException | TemplateException | ResourceException e) {
			log.error("Failed to send email", e);
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

	@Override
	public void sendMessage(String resourceKey, Realm currentRealm, ITokenResolver ticketResolver,
			Iterator<Principal> principals, Collection<String> emails) {
		sendMessage(resourceKey, currentRealm, ticketResolver, principals, emails, new Date());
	}
	
	@Override
	public void sendMessageNow(String resourceKey, Realm currentRealm, ITokenResolver ticketResolver,
			Iterator<Principal> principals, Collection<String> emails) {
		sendMessage(resourceKey, currentRealm, ticketResolver, principals, emails, null);
	}

}
