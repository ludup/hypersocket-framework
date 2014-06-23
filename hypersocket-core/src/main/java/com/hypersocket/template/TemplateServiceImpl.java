package com.hypersocket.template;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.i18n.I18NService;
import com.hypersocket.menus.MenuRegistration;
import com.hypersocket.menus.MenuService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;

@Service
public class TemplateServiceImpl extends AbstractResourceServiceImpl<Template>
		implements TemplateService {

	public static final String EMAIL_TEMPLATE = "template.email";

	public static final String RESOURCE_BUNDLE = "TemplateService";

	Set<String> templateTypes = new HashSet<String>();

	@Autowired
	I18NService i18nService;

	@Autowired
	TemplateRepository repository;

	@Autowired
	MenuService menuService;

	@PostConstruct
	private void postConstruct() {

		i18nService.registerBundle(RESOURCE_BUNDLE);

		templateTypes.add(EMAIL_TEMPLATE);

		menuService.registerMenu(new MenuRegistration(RESOURCE_BUNDLE,
				"templates", "fa-file-code-o", "templates", 9999,
				TemplatePermission.READ, TemplatePermission.CREATE,
				TemplatePermission.UPDATE, TemplatePermission.DELETE),
				MenuService.MENU_RESOURCES);
	}

	public void registerTemplateType(String type) {

		if (templateTypes.contains(type)) {
			throw new IllegalStateException(type + " already registered");
		}
		templateTypes.add(type);
	}

	@Override
	protected AbstractResourceRepository<Template> getRepository() {
		return repository;
	}

	@Override
	protected String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public Class<? extends PermissionType> getPermissionType() {
		return TemplatePermission.class;
	}

	@Override
	protected void fireResourceCreationEvent(Template resource) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void fireResourceCreationEvent(Template resource, Throwable t) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void fireResourceUpdateEvent(Template resource) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void fireResourceUpdateEvent(Template resource, Throwable t) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void fireResourceDeletionEvent(Template resource) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void fireResourceDeletionEvent(Template resource, Throwable t) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set<String> getRegisteredTypes() {
		return new HashSet<String>(templateTypes);
	}

	@Override
	public Template createTemplate(String name, String template, String type)
			throws ResourceCreationException, AccessDeniedException {

		Template t = new Template();
		t.setName(name);
		t.setTemplate(template);
		t.setType(type);

		createResource(t);
		return t;
	}

	@Override
	public void updateTemplate(Template t, String name, String template,
			String type) throws ResourceChangeException, AccessDeniedException {

		t.setName(name);
		t.setTemplate(template);
		t.setType(type);

		updateResource(t);

	}

}
