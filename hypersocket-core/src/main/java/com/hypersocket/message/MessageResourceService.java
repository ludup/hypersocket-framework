package com.hypersocket.message;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.upload.FileUpload;

public interface MessageResourceService extends
		AbstractResourceService<MessageResource> {

	MessageResource updateResource(MessageResource resourceById, String name, Map<String,String> properties)
			throws ResourceException, AccessDeniedException;

	MessageResource createResource(String name, Realm realm, Map<String,String> properties)
			throws ResourceException, AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate() throws AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate(MessageResource resource)
			throws AccessDeniedException;

	void registerI18nMessage(String resourceBundle, String resourceKey, Set<String> variables);

	MessageResource createResource(String resourceKey, String name, String subject, String body, String html,
			Set<String> variables, Boolean enabled, Boolean track, 
				Collection<FileUpload> attachments, Realm realm, EmailDeliveryStrategy delivery)
			throws ResourceException, AccessDeniedException;

	Set<String> getMessageVariables(MessageResource message);

	MessageResource getMessageById(String resourceKey, Realm realm);
	
	void registerI18nMessage(String resourceBundle, String resourceKey, Set<String> variables,
			boolean system);

	void registerI18nMessage(String resourceBundle, String resourceKey, Set<String> variables,
			boolean system, MessageTemplateRepository repository, boolean enabled);

	void registerI18nMessage(String resourceBundle, String resourceKey, Set<String> variables,
			boolean system, MessageTemplateRepository repository);

	void registerI18nMessage(String resourceBundle, String resourceKey, Set<String> variables,
			boolean system, MessageTemplateRepository repository, boolean enabled, EmailDeliveryStrategy delivery);

	void test(MessageResource resourceById, String email) throws ResourceNotFoundException, AccessDeniedException;

	MessageSender newMessageSender(Realm realm);

}
