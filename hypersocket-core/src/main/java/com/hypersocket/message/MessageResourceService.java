package com.hypersocket.message;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.upload.FileUpload;
import com.hypersocket.utils.ITokenResolver;

public interface MessageResourceService extends
		AbstractResourceService<MessageResource> {

	MessageResource updateResource(MessageResource resourceById, String name, Map<String,String> properties)
			throws ResourceChangeException, AccessDeniedException;

	MessageResource createResource(String name, Realm realm, Map<String,String> properties)
			throws ResourceCreationException, AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate() throws AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate(MessageResource resource)
			throws AccessDeniedException;

	void sendMessage(Integer messageId, Realm realm, ITokenResolver tokenResolver, Principal... principals);
	
	void sendMessage(Integer messageId, Realm realm, ITokenResolver tokenResolver, Collection<Principal> principals);

	void registerI18nMessage(Integer messageId, String resourceBundle, String resourceKey, Set<String> variables);

	MessageResource createResource(Integer messageId, String name, String subject, String body, String html,
			Set<String> variables, Boolean enabled, Boolean track, Collection<FileUpload> attachments, Realm realm)
			throws ResourceCreationException, AccessDeniedException;

	Set<String> getMessageVariables(MessageResource message);

	void registerI18nMessage(Integer messageId, String resourceBundle, String resourceKey, Set<String> variables,
			boolean system);

	void sendMessageToEmailAddress(Integer messageId, Realm realm, ITokenResolver tokenResolver, Collection<String> emails);
	
	void sendMessageToEmailAddress(Integer messageId, Realm realm, ITokenResolver tokenResolver, String... emails);

}
