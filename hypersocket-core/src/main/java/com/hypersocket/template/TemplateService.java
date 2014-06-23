package com.hypersocket.template;

import java.util.Set;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.AbstractResourceService;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;

public interface TemplateService extends AbstractResourceService<Template> {

	Set<String> getRegisteredTypes();

	Template createTemplate(String name, String template, String type) throws ResourceCreationException, AccessDeniedException;

	void updateTemplate(Template template, String name, String template2,
			String type) throws ResourceChangeException, AccessDeniedException;

}
