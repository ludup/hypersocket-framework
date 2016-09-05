package com.hypersocket.resource;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.PermissionType;

@Service
public class ResourceGroupServiceImpl extends AbstractResourceServiceImpl<ResourceGroup>
		implements ResourceGroupService {

	static Logger log = LoggerFactory.getLogger(ResourceGroupServiceImpl.class);

	public static final String RESOURCE_BUNDLE = "ResourceGroupService";
	@Autowired
	I18NService i18nService;

	@Autowired
	ResourceGroupRepository repository;

	public ResourceGroupServiceImpl() {
		super("resourceGroup");
		setAssertPermissions(false);
	}

	@PostConstruct
	private void postConstruct() {
		i18nService.registerBundle(RESOURCE_BUNDLE);
	}

	@Override
	public void registerResourceGroup(ResourceGroup condition) {
	}

	@Override
	protected AbstractResourceRepository<ResourceGroup> getRepository() {
		return repository;
	}

	@Override
	protected String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public Class<? extends PermissionType> getPermissionType() {
		throw new UnsupportedOperationException();
	}

	@Override
	protected Class<ResourceGroup> getResourceClass() {
		return ResourceGroup.class;
	}

	@Override
	protected void fireResourceCreationEvent(ResourceGroup resource) {
		throw new UnsupportedOperationException();
	}

	@Override
	protected void fireResourceCreationEvent(ResourceGroup resource, Throwable t) {
		throw new UnsupportedOperationException();

	}

	@Override
	protected void fireResourceUpdateEvent(ResourceGroup resource) {
		throw new UnsupportedOperationException();

	}

	@Override
	protected void fireResourceUpdateEvent(ResourceGroup resource, Throwable t) {
		throw new UnsupportedOperationException();

	}

	@Override
	protected void fireResourceDeletionEvent(ResourceGroup resource) {
		throw new UnsupportedOperationException();

	}

	@Override
	protected void fireResourceDeletionEvent(ResourceGroup resource, Throwable t) {
		throw new UnsupportedOperationException();

	}

}
