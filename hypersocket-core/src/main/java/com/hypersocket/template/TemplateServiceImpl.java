package com.hypersocket.template;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.mail.Message.RecipientType;

import org.codemonkey.simplejavamail.Recipient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.email.EmailNotificationService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.realm.MediaNotFoundException;
import com.hypersocket.realm.MediaType;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.replace.ReplacementUtils;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.tables.ColumnSort;

@Service
public class TemplateServiceImpl extends AbstractResourceServiceImpl<Template>
		implements TemplateService {

	public static final String RESOURCE_BUNDLE = "TemplateService";

	Set<String> templateTypes = new HashSet<String>();

	@Autowired
	I18NService i18nService;

	@Autowired
	TemplateRepository repository;

	@Autowired
	EmailNotificationService emailService;

	@Autowired
	RealmService realmService; 
	
	@PostConstruct
	private void postConstruct() {

		i18nService.registerBundle(RESOURCE_BUNDLE);
		
	}

	@Override
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
	public void emailTemplate(Template template, Principal principal,
			Map<String, String> replacements) throws ResourceNotFoundException,
			MediaNotFoundException {

		String email = realmService.getPrincipalAddress(principal, MediaType.EMAIL);
		String subject = ReplacementUtils.processTokenReplacements(
				template.getSubject(), replacements);
		String text = ReplacementUtils.processTokenReplacements(
				template.getTemplate(), replacements);

		emailService.sendHtmlEmail(subject, text,
				new Recipient[] { new Recipient(realmService.getPrincipalDescription(principal),
						email, RecipientType.TO) });

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
	public Template createTemplate(String name, String subject, String template, String type)
			throws ResourceCreationException, AccessDeniedException {

		Template t = new Template();
		t.setName(name);
		t.setSubject(subject);
		t.setTemplate(template);
		t.setType(type);

		createResource(t);
		return t;
	}

	@Override
	public void updateTemplate(Template t, String name, String subject, String template,
			String type) throws ResourceChangeException, AccessDeniedException {

		t.setName(name);
		t.setSubject(subject);
		t.setTemplate(template);
		t.setType(type);

		updateResource(t);

	}

	@Override
	public List<Template> searchResources(Realm realm, String search, String type, int start, int length,
			ColumnSort[] sorting) throws AccessDeniedException {
		
		assertPermission(TemplatePermission.READ);
		
		return repository.search(realm, search, type, start, length, sorting);
	}

	@Override
	public long getResourceCount(Realm realm, String search, String type)
			throws AccessDeniedException {
		
		assertPermission(TemplatePermission.READ);
		
		return repository.getResourceCount(realm, search, type);
	}
}
