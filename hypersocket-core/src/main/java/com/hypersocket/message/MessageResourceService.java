package com.hypersocket.message;

import java.util.Collection;
import java.util.Date;
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

	void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver, Principal... principals) throws ResourceException;
	
	void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver, Collection<Principal> principals) throws ResourceException;

	void registerI18nMessage(String resourceBundle, String resourceKey, Set<String> variables);

	MessageResource createResource(String resourceKey, String name, String subject, String body, String html,
			Set<String> variables, Boolean enabled, Boolean track, 
				Collection<FileUpload> attachments, Realm realm, EmailDeliveryStrategy delivery)
			throws ResourceException, AccessDeniedException;

	Set<String> getMessageVariables(MessageResource message);

	MessageResource getMessageById(String resourceKey, Realm realm);
	
	void registerI18nMessage(String resourceBundle, String resourceKey, Set<String> variables,
			boolean system);

	void sendMessageToEmailAddress(String resourceKey, Realm realm, Collection<RecipientHolder> emails, ITokenResolver tokenResolver);

	void sendMessageToEmailAddress(String resourceKey, Realm realm, ITokenResolver tokenResolver, Collection<String> emails, List<EmailAttachment> attachments);
	
	void sendMessageToEmailAddress(String resourceKey, Realm realm, ITokenResolver tokenResolver, String... emails);

	void registerI18nMessage(String resourceBundle, String resourceKey, Set<String> variables,
			boolean system, MessageTemplateRepository repository, boolean enabled);

	void registerI18nMessage(String resourceBundle, String resourceKey, Set<String> variables,
			boolean system, MessageTemplateRepository repository);

	void registerI18nMessage(String resourceBundle, String resourceKey, Set<String> variables,
			boolean system, MessageTemplateRepository repository, boolean enabled, EmailDeliveryStrategy delivery);

	void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver, Collection<Principal> principals,
			Date schedule);

	void sendMessageNow(String resourceKey, Realm realm, ITokenResolver tokenResolver, Collection<Principal> principals);

	void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver, RecipientHolder replyTo,
			List<EmailAttachment> attachments, Collection<Principal> principals);

	void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver, RecipientHolder replyTo,
			Collection<Principal> principals, Date schedule, List<EmailAttachment> attachments);

	void sendMessage(String resourceKey, Realm realm, ITokenResolver tokenResolver, RecipientHolder replyTo,
			List<EmailAttachment> attachments, Principal... principals);

	void sendMessageToEmailAddress(String resourceKey, Realm realm, Collection<RecipientHolder> recipients,
			RecipientHolder replyTo, ITokenResolver tokenResolver, List<EmailAttachment> attachments);

}
