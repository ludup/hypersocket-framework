package com.hypersocket.attributes;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.attributes.events.AttributeCategoryCreatedEvent;
import com.hypersocket.attributes.events.AttributeCategoryDeletedEvent;
import com.hypersocket.attributes.events.AttributeCategoryEvent;
import com.hypersocket.attributes.events.AttributeCategoryUpdatedEvent;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.ResourceTemplateRepositoryImpl;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.TransactionAdapter;

@Service
public abstract class AbstractAttributeCategoryServiceImpl<A extends AbstractAttribute<T>, T extends RealmAttributeCategory<A>> extends AbstractResourceServiceImpl<T> implements
		AttributeCategoryService<A, T> {

	public static final String RESOURCE_BUNDLE = "UserAttributes";

	@Autowired
	I18NService i18nService;

	@Autowired
	EventService eventService;

	// Should be set by sub-class
	protected AttributeCategoryRepository<T> attributeCategoryRepository;

	Map<String, PropertyCategory> activeCategories = new HashMap<String, PropertyCategory>();

	private PermissionType readPermission;
	private Class<? extends PermissionType> permissionType;
	private PermissionType updatePermission;
	private Class<T> resourceClass;
	private String resourceBundle;
	private PermissionType deletePermission;
	private PermissionType createPermission;
	
	protected AbstractAttributeCategoryServiceImpl(String resourceBundle, Class<? extends PermissionType> permissionType, Class<T> resourceClass, PermissionType createPermission, PermissionType readPermission, PermissionType updatePermission, PermissionType deletePermission) {
		super("attributeCategory");
		this.resourceBundle = resourceBundle;
		this.resourceClass = resourceClass;
		this.createPermission = createPermission;
		this.readPermission = readPermission;
		this.deletePermission = deletePermission;
		this.updatePermission = updatePermission;
		this.permissionType = permissionType;
	}
	
	protected void init() {
		// Only generic events for now, individual events seems excessive
		eventService.registerEvent(AttributeCategoryEvent.class,
				RESOURCE_BUNDLE);
		eventService.registerEvent(AttributeCategoryCreatedEvent.class,
				RESOURCE_BUNDLE);
		eventService.registerEvent(AttributeCategoryUpdatedEvent.class,
				RESOURCE_BUNDLE);
		eventService.registerEvent(AttributeCategoryDeletedEvent.class,
				RESOURCE_BUNDLE);
		
		for(T cat : attributeCategoryRepository.allResources()) {
			registerPropertyCategory(cat);
		}
	}

	@Override
	public Collection<String> getContexts() {
		return ResourceTemplateRepositoryImpl.getContextNames();
	}


	@Override
	public T createAttributeCategory(String name,
			int weight) throws ResourceCreationException,
			AccessDeniedException {
		assertPermission(createPermission);

		if (attributeCategoryRepository.getResourceByName(name, getCurrentRealm())!=null) {
			throw new ResourceCreationException(resourceBundle,
					"attribute.catInUse.error", name);
		}

		T attributeCategory = newAttributeCategoryInstance();
		attributeCategory.setName(name);
		attributeCategory.setRealm(getCurrentRealm());
		attributeCategory.setWeight(weight);
		
		createResource(attributeCategory, new TransactionAdapter<T>() {
			@Override
			public void afterOperation(T resource, Map<String,String> properties) {
				registerPropertyCategory(resource);
			}
		});


		return attributeCategory;

	}

	protected abstract T newAttributeCategoryInstance();

	@Override
	public T updateAttributeCategory(T category, String name,
			int weight) throws ResourceChangeException, AccessDeniedException {
		assertPermission(updatePermission);

		T current = attributeCategoryRepository.getResourceByName(name, getCurrentRealm());
		
		if(current!=null && !current.getId().equals(category.getId())) {
			throw new ResourceChangeException(resourceBundle,
					"attribute.nameInUse.error", name);
		}

		category.setName(name);
		category.setWeight(weight);
		
		updateResource(category, new TransactionAdapter<T>() {
			@Override
			public void afterOperation(T resource, Map<String,String> properties) {
				registerPropertyCategory(resource);
			}
		});

		return category;


	}

	@Override
	public void deleteAttributeCategory(T category)
			throws AccessDeniedException, ResourceChangeException {

		assertPermission(deletePermission);
		
		for(AbstractAttribute<?> attr : category.getAttributes()) {
			if(!attr.isDeleted()) {
				throw new ResourceChangeException(resourceBundle, "error.hasAttributes", category.getName());
			}
		}
		deleteResource(category);

	}
	
	@Override
	public Long getMaximumCategoryWeight() throws AccessDeniedException {
		assertPermission(readPermission);
		return attributeCategoryRepository.getMaximumCategoryWeight();
	}


	@Override
	protected AbstractResourceRepository<T> getRepository() {
		return attributeCategoryRepository;
	}

	@Override
	protected String getResourceBundle() {
		return resourceBundle;
	}

	@Override
	public Class<? extends PermissionType> getPermissionType() {
		return permissionType;
	}

	@Override
	protected Class<T> getResourceClass() {
		return resourceClass;
	}

	@Override
	protected void fireResourceCreationEvent(T resource) {
		eventService.publishEvent(new AttributeCategoryCreatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceCreationEvent(T resource, Throwable t) {
		eventService.publishEvent(new AttributeCategoryCreatedEvent(this, t,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceUpdateEvent(T resource) {
		eventService.publishEvent(new AttributeCategoryUpdatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceUpdateEvent(T resource, Throwable t) {
		eventService.publishEvent(new AttributeCategoryUpdatedEvent(this, t,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceDeletionEvent(T resource) {
		eventService.publishEvent(new AttributeCategoryDeletedEvent(this,
				getCurrentSession(), resource));	
	}

	@Override
	protected void fireResourceDeletionEvent(T resource, Throwable t) {
		eventService.publishEvent(new AttributeCategoryDeletedEvent(this, t,
				getCurrentSession(), resource));	
	}
	
	@Override
	public Collection<PropertyCategory> getPropertyCategories() {
		return activeCategories.values();
	}
	
	@Override
	public PropertyCategory getCategoryByResourceKey(String resourceKey) {
		return activeCategories.get(resourceKey);
	}
	
	@Override
	public PropertyCategory registerPropertyCategory(T c) {
		return registerPropertyCategory(
				"attributeCategory" + String.valueOf(c.getId()),
				resourceBundle, c.getWeight(), c.isHidden(), c.getName(), c.getVisibilityDependsOn(), c.getVisibilityDependsValue());
	}
	
	protected PropertyCategory registerPropertyCategory(String resourceKey,
			String bundle, int weight, boolean hidden, String name, String visibilityDependsOn, String visibilityDependsValue) {

		PropertyCategory category = new PropertyCategory();
		category.setName(name);
		category.setBundle(bundle);
		category.setCategoryKey(resourceKey);
		category.setDisplayMode("");
		category.setWeight(weight);
		category.setUserCreated(true);
		category.setFilter("custom");
		category.setHidden(hidden);
		category.setVisibilityDependsOn(visibilityDependsOn);
		category.setVisibilityDependsValue(visibilityDependsValue);
		activeCategories.put(category.getCategoryKey(), category);
		return category;
	}

}
