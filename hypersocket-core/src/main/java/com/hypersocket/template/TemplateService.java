package com.hypersocket.template;

import java.util.Map;
import java.util.Set;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.MediaNotFoundException;
import com.hypersocket.realm.Principal;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceNotFoundException;

public interface TemplateService extends AbstractResourceService<Template> {

	Set<String> getRegisteredTypes();

	Template createTemplate(String name, String subject, String template, String type) throws ResourceCreationException, AccessDeniedException;

	void updateTemplate(Template template, String name, String subject, String template2,
			String type) throws ResourceChangeException, AccessDeniedException;

	void emailTemplate(String templateName, Principal principal,
			Map<String, String> replacements) throws ResourceNotFoundException,
			MediaNotFoundException;

}
