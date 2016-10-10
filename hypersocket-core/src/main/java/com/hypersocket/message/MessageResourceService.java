package com.hypersocket.message;

import java.util.Collection;
import java.util.Map;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.upload.FileUpload;

public interface MessageResourceService extends
		AbstractResourceService<MessageResource> {

	MessageResource updateResource(MessageResource resourceById, String name, Map<String,String> properties)
			throws ResourceChangeException, AccessDeniedException;

	MessageResource createResource(String name, Realm realm, Map<String,String> properties)
			throws ResourceCreationException, AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate() throws AccessDeniedException;

	Collection<PropertyCategory> getPropertyTemplate(MessageResource resource)
			throws AccessDeniedException;

	MessageResource createResource(String name, String subject, String body, String html, Boolean enabled,
<<<<<<< Updated upstream
			Boolean track, Class<? extends SystemEvent> fireEvent, Collection<FileUpload> attachments, Realm realm,
			Map<String, String> properties) throws ResourceCreationException, AccessDeniedException;
=======
			Boolean track, Collection<FileUpload> attachments, Realm realm, Map<String, String> properties)
			throws ResourceCreationException, AccessDeniedException;
>>>>>>> Stashed changes

}
