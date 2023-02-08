package com.hypersocket.message;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.simplejavamail.MailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.email.EmailAttachment;
import com.hypersocket.email.EmailBatchService;
import com.hypersocket.email.EmailNotificationBuilder;
import com.hypersocket.email.RecipientHolder;
import com.hypersocket.messagedelivery.AbstractEMailMessageDeliveryProvider;
import com.hypersocket.messagedelivery.MessageDeliveryService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.ResourceUtils;
import com.hypersocket.realm.MediaType;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.ServerResolver;
import com.hypersocket.realm.UserPrincipal;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.triggers.ValidationException;
import com.hypersocket.upload.FileUpload;
import com.hypersocket.upload.FileUploadService;
import com.hypersocket.util.CompoundIterator;
import com.hypersocket.util.FilteringIterator;
import com.hypersocket.util.ProxiedIterator;
import com.hypersocket.util.SingleItemIterator;
import com.hypersocket.util.TransformingIterator;
import com.hypersocket.utils.ITokenResolver;

import freemarker.template.Template;
import freemarker.template.TemplateException;

public class MessageSender {

	static Logger log = LoggerFactory.getLogger(MessageSender.class);
	
	private final Realm realm;
	private final RealmService realmService;
	private final FreeMarkerService templateService;
	private final EmailBatchService batchService;
	private final MessageDeliveryService messageDeliveryService;
	private final FileUploadService uploadService;
	private final MessageResourceService messageResourceService;
	
	private String messageResourceKey;
	private MessageResource messageResource;
	private ITokenResolver tokenResolver;
	private RecipientHolder replyTo;
	private Iterator<RecipientHolder> recipients;
	private Iterator<Principal> principals;
	private List<EmailAttachment> attachments = Collections.emptyList();
	private String context;
	private Date schedule;
	private Iterator<String> recipientAddresses;
	private Optional<String> providerResourceKey = Optional.empty();
	private boolean ignoreDisabledFlag;

	MessageSender(Realm realm, RealmService realmService, FreeMarkerService templateService, EmailBatchService batchService, MessageDeliveryService messageDeliveryService, FileUploadService uploadService, MessageResourceService messageResourceService) {
		this.realm = realm;
		this.realmService = realmService;
		this.templateService = templateService;
		this.batchService = batchService;
		this.messageDeliveryService = messageDeliveryService;
		this.uploadService = uploadService;
		this.messageResourceService = messageResourceService;
	}
	
	public boolean isIgnoreDisabledFlag() {
		return ignoreDisabledFlag;
	}

	public MessageSender ignoreDisabledFlag(boolean ignoreDisabledFlag) {
		this.ignoreDisabledFlag = ignoreDisabledFlag;
		return this;
	}

	public Optional<String> provider() {
		return providerResourceKey;
	}

	public MessageSender provider(String providerResourceKey) {
		return provider(StringUtils.isBlank(providerResourceKey) ? Optional.empty() : Optional.of(providerResourceKey));
	}

	public MessageSender provider(Optional<String> providerResourceKey) {
		this.providerResourceKey = providerResourceKey;
		return this;
	}

	public Date schedule() {
		return schedule;
	}

	public MessageSender sendNow() {
		this.schedule = null;
		return this;
	}

	public MessageSender batch() {
		this.schedule = new Date();
		return this;
	}

	public MessageSender batchFor(Date schedule) {
		this.schedule = schedule;
		return this;
	}

	public ITokenResolver tokenResolver() {
		return tokenResolver;
	}

	public MessageSender tokenResolver(ITokenResolver tokenResolver) {
		this.tokenResolver = tokenResolver;
		return this;
	}

	public RecipientHolder replyTo() {
		return replyTo;
	}

	public MessageSender replyTo(RecipientHolder replyTo) {
		this.replyTo = replyTo;
		return this;
	}

	public Iterator<RecipientHolder> recipients() {
		return recipients;
	}

	public MessageSender recipients(Iterator<RecipientHolder> recipients) {
		this.recipients = recipients;
		return this;
	}

	public Iterator<Principal> principals() {
		return principals;
	}

	public MessageSender principals(Iterator<Principal> principals) {
		this.principals = principals;
		return this;
	}

	public MessageSender principals(Collection<Principal> principals) {
		return principals(principals == null ? null : principals.iterator());
	}

	public MessageSender principals(Principal... principals) {
		return principals(Arrays.asList(principals));
	}
	
	public Iterator<String> recipientAddresses() {
		return recipientAddresses;
	}
	
	public MessageSender recipientAddress(String recipientAddress) {
		return recipientAddresses(recipientAddress == null ? null : new SingleItemIterator<String>(recipientAddress)); 
	}
	
	public MessageSender recipientAddresses(Iterator<String> recipientAddresses) {
		this.recipientAddresses = recipientAddresses;
		return this;
	}
	
	public MessageSender recipientAddresses(String... recipientAddresses) {
		return recipientAddresses(Arrays.asList(recipientAddresses));
	}
	
	public MessageSender recipientAddresses(Collection<String> recipientAddresses) {
		return recipientAddresses(recipientAddresses == null ? null : recipientAddresses.iterator());
	}

	public MessageSender recipients(Collection<RecipientHolder> recipients) {
		return recipients(recipients == null ? null : recipients.iterator());
	}

	public MessageSender recipients(RecipientHolder... recipients) {
		return recipients(Arrays.asList(recipients));
	}

	public List<EmailAttachment> attachments() {
		return attachments;
	}

	public MessageSender attachments(List<EmailAttachment> attachments) {
		this.attachments = attachments;
		return this;
	}

	public String context() {
		return context;
	}

	public MessageSender context(String context) {
		this.context = context;
		return this;
	}

	public Realm realm() {
		return realm;
	}

	public String messageResourceKey() {
		return messageResourceKey;
	}

	public MessageSender messageResourceKey(String messageResourceKey) {
		if(messageResource != null)
			throw new IllegalStateException("Can have either a messageResourceKey() or a messageResource(), but not both.");
		this.messageResourceKey = messageResourceKey;
		return this;
	}

	public MessageResource messageResource() {
		return messageResource;
	}

	public MessageSender messageResource(MessageResource messageResource) {
		if(messageResourceKey != null)
			throw new IllegalStateException("Can have either a messageResourceKey() or a messageResource(), but not both.");
		this.messageResource = messageResource;
		return this;
	}

	public boolean send()  {
		try {
			sendOrError();
			return true;
		}
		catch(Exception ioe) {
			//			
			return false;
		}
	}
	
	public void sendOrError() throws Exception {
		try {
			var message = messageResource;
			if(message == null) {
				if(messageResourceKey == null)
					throw new IllegalStateException("Must set either a messageResourceKey or a messageResource to be able to send a message.");
					
				message = messageResourceService.getMessageById(messageResourceKey, realm);
				if (message == null) {
					throw new IllegalStateException(String.format("Invalid message id %s", messageResourceKey));
				}
			}
				
			sendMessage(message);
		}
		catch(IOException | TemplateException | ValidationException | AccessDeniedException | ResourceException | IllegalStateException e) {
			log.error("Failed to send email.", e);
			throw e;
		}
	}


	private void sendMessage(MessageResource message) throws IOException, TemplateException, ValidationException, AccessDeniedException, ResourceException {
		var strategy = message.getDeliveryStrategy();
		var recipients = new CompoundIterator<RecipientHolder>();
		
		if(this.recipients != null) {
			recipients.addIterator(this.recipients);
		}
		
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

		
		if (recipientAddresses != null) {
			/**
			 * LDP discovered issue where a blank value was in this collection. 
			 * Guard against that here.
			 */
			recipients.addIterator(new TransformingIterator<String, RecipientHolder>(new FilteringIterator<>(recipientAddresses) {
				@Override
				protected boolean isInclude(String item) {
					return StringUtils.isNotBlank(item);
				}
			}) {
				@Override
				protected RecipientHolder transform(String email) {
					return RecipientHolder.ofEmailAddressSpec(email.toLowerCase());
				}
			});
		}

		sendMessage(message, recipients);
	}

	private void sendMessage(MessageResource message, Iterator<RecipientHolder> recipients) throws IOException, TemplateException, ValidationException, AccessDeniedException, ResourceException {

		if (!ignoreDisabledFlag && !message.getEnabled()) {
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
					return RecipientHolder.ofAddress(from);
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

				if(StringUtils.isBlank(recipient.getAddress())) {
					log.warn("Detected empty email in a RecipientHolder! Skipping");
					continue;
				}
				
				if(processedEmails.contains(recipient.getAddress().toLowerCase())) {
					log.info("Skipping {} because we already sent this message to that address", recipient.getAddress());
					continue;
				}
				
				processedEmails.add(recipient.getAddress().toLowerCase());
				
				Map<String, Object> data = tokenResolver.getData();
				data.putAll(new ServerResolver(realm).getData());
				data.put("email", recipient.getAddress());
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
					
					var provider = (AbstractEMailMessageDeliveryProvider<EmailNotificationBuilder>)messageDeliveryService.getProviderOrBest(MediaType.EMAIL, providerResourceKey.orElse(""), EmailNotificationBuilder.class);
					var builder = provider.newBuilder(realm);
					builder.subject(subjectWriter.toString());
					builder.text(bodyWriter.toString());
					builder.html(htmlWriter.toString());
					builder.replyToName(replyTo != null ? replyTo.getName() : message.getReplyToName());
					builder.replyToEmail(replyTo != null ? replyTo.getAddress() : message.getReplyToEmail());
					builder.recipient(recipient);
					builder.archive(message.getArchive());
					builder.track(message.getTrack());
					builder.delay(50);
					builder.context(context);
					builder.attachments(emailAttachments);
					builder.send();
				} else {
					attachmentsListString = ResourceUtils.implodeValues(attachmentUUIDs);

					batchService.scheduleEmail(realm, providerResourceKey, subjectWriter.toString(), bodyWriter.toString(),
							htmlWriter.toString(), replyTo != null ? replyTo.getName() : message.getReplyToName(),
							replyTo != null ? replyTo.getAddress() : message.getReplyToEmail(), recipient.getName(),
							recipient.getAddress(), message.getArchive(), message.getTrack(), attachmentsListString, schedule, context);

				}
			}

		} catch (MailException e) {
			// Will be logged by mail API
		} 

	}
}
