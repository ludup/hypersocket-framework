package com.hypersocket.message;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.hypersocket.email.EmailAttachment;
import com.hypersocket.email.RecipientHolder;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.upload.FileUpload;
import com.hypersocket.utils.ITokenResolver;

public interface MessageResourceService extends
		AbstractResourceService<MessageResource> {

	MessageResource updateResource(MessageResource resourceById, String name, Map<String,String> properties)
			throws ResourceException, AccessDeniedException;

	MessageResource createResource(String name, Realm realm, Map<String,String> properties)
			throws ResourceException, AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate() throws AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate(MessageResource resource)
			throws AccessDeniedException;

	@Deprecated
	void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver, Principal... principals) throws ResourceException;

	@Deprecated
	void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver, Collection<Principal> principals) throws ResourceException;

	@Deprecated
	void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver, Iterator<Principal> principals) throws ResourceException;

	void registerI18nMessage(String resourceBundle, String resourceKey, Set<String> variables);

	MessageResource createResource(String resourceKey, String name, String subject, String body, String html,
			Set<String> variables, Boolean enabled, Boolean track, 
				Collection<FileUpload> attachments, Realm realm, EmailDeliveryStrategy delivery)
			throws ResourceException, AccessDeniedException;

	Set<String> getMessageVariables(MessageResource message);

	MessageResource getMessageById(String resourceKey, Realm realm);
	
	void registerI18nMessage(String resourceBundle, String resourceKey, Set<String> variables,
			boolean system);

	@Deprecated
	void sendMessageToEmailAddress(String resourceKey, Realm realm, Collection<RecipientHolder> emails, ITokenResolver tokenResolver);

	@Deprecated
	void sendMessageToEmailAddress(String resourceKey, Realm realm, ITokenResolver tokenResolver, Collection<String> emails, List<EmailAttachment> attachments, String context);

	@Deprecated
	void sendMessageToEmailAddress(String resourceKey, Realm realm, ITokenResolver tokenResolver, String... emails);

	void registerI18nMessage(String resourceBundle, String resourceKey, Set<String> variables,
			boolean system, MessageTemplateRepository repository, boolean enabled);

	void registerI18nMessage(String resourceBundle, String resourceKey, Set<String> variables,
			boolean system, MessageTemplateRepository repository);

	void registerI18nMessage(String resourceBundle, String resourceKey, Set<String> variables,
			boolean system, MessageTemplateRepository repository, boolean enabled, EmailDeliveryStrategy delivery);

	@Deprecated
	void sendMessageNow(String resourceKey, Realm realm, ITokenResolver tokenResolver, Collection<Principal> principals);

	@Deprecated
	void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver, Iterator<Principal> principals,
			Collection<String> emails, Date schedule);

	@Deprecated
	void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver, RecipientHolder replyTo,
			List<EmailAttachment> attachments, Iterator<Principal> principals, Collection<String> emails, String context);

	@Deprecated
	void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver, RecipientHolder replyTo,
			Iterator<Principal> principals, Collection<String> emails, Date schedule, List<EmailAttachment> attachments);

	@Deprecated
	void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver, RecipientHolder replyTo,
			List<EmailAttachment> attachments, Iterator<Principal> principals, String context);

	@Deprecated
	void sendMessageToEmailAddress(String resourceKey, Realm realm, Collection<RecipientHolder> recipients,
			RecipientHolder replyTo, ITokenResolver tokenResolver, List<EmailAttachment> attachments, String context);

	@Deprecated
	void sendMessage(String message, Realm currentRealm, ITokenResolver resolver,
			Iterator<Principal> principals, Collection<String> emails);

	@Deprecated
	void sendMessage(MessageResource message, Realm realm, ITokenResolver tokenResolver, RecipientHolder replyTo,
			Iterator<Principal> principals, Collection<String> emails, Date schedule,
			List<EmailAttachment> attachments, String context);

	@Deprecated
	void sendMessageNow(String resourceKey, Realm currentRealm, ITokenResolver ticketResolver,
			Iterator<Principal> principals, Collection<String> emails);

	void test(MessageResource resourceById, String email) throws ResourceNotFoundException, AccessDeniedException;

	@Deprecated
	void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver, RecipientHolder replyTo,
			Collection<Principal> principals);

	@Deprecated
	void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver, RecipientHolder recipient);

	MessageSender newMessageSender(Realm realm);

}
