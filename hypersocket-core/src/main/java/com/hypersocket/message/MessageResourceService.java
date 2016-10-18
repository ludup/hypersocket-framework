package com.hypersocket.message;

import java.util.Collection;
import java.util.Map;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceNotFoundException;
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

	MessageResource createResource(Integer mesasgeId, String name, String subject, String body, String html, Boolean enabled,
			Boolean track, Collection<FileUpload> attachments, Realm realm)
			throws ResourceCreationException, AccessDeniedException;

	MessageResource createResource(Integer mesasgeId, String name, String subject, String body, String html, Boolean enabled,
			Boolean track, Realm realm) throws ResourceCreationException, AccessDeniedException;

	MessageResource createResource(Integer mesasgeId, String name, String subject, String body, Realm realm)
			throws ResourceCreationException, AccessDeniedException;

	void sendMessage(Integer messageId, Realm realm, ITokenResolver tokenResolver, Principal... principals)
			throws ResourceNotFoundException, AccessDeniedException;

}
