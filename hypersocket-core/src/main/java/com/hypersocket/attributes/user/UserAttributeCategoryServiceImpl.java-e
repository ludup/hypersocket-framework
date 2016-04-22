package com.hypersocket.attributes.user;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.attributes.user.events.UserAttributeCategoryCreatedEvent;
import com.hypersocket.attributes.user.events.UserAttributeCategoryDeletedEvent;
import com.hypersocket.attributes.user.events.UserAttributeCategoryEvent;
import com.hypersocket.attributes.user.events.UserAttributeCategoryUpdatedEvent;
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
public class UserAttributeCategoryServiceImpl extends AbstractResourceServiceImpl<UserAttributeCategory> implements
		UserAttributeCategoryService {

	public static final String RESOURCE_BUNDLE = "UserAttributes";

	@Autowired
	I18NService i18nService;

	@Autowired
	UserAttributeCategoryRepository attributeCategoryRepository;

	@Autowired
	UserAttributeService userAttributeService;
	
	@Autowired
	EventService eventService;

	Map<String, PropertyCategory> activeCategories = new HashMap<String, PropertyCategory>();
	
	public UserAttributeCategoryServiceImpl() {
		super("attributeCategory");
	}
	
	@PostConstruct
	private void postConstruct() {

		eventService.registerEvent(UserAttributeCategoryEvent.class,
				RESOURCE_BUNDLE);
		eventService.registerEvent(UserAttributeCategoryCreatedEvent.class,
				RESOURCE_BUNDLE);
		eventService.registerEvent(UserAttributeCategoryUpdatedEvent.class,
				RESOURCE_BUNDLE);
		eventService.registerEvent(UserAttributeCategoryDeletedEvent.class,
				RESOURCE_BUNDLE);
	}

	@Override
	public Collection<String> getContexts() {
		return ResourceTemplateRepositoryImpl.getContextNames();
	}


	@Override
	public UserAttributeCategory createAttributeCategory(String name,
			int weight) throws ResourceCreationException,
			AccessDeniedException {
		assertPermission(UserAttributePermission.CREATE);

		if (attributeCategoryRepository.getResourceByName(name, getCurrentRealm())!=null) {
			throw new ResourceCreationException(RESOURCE_BUNDLE,
					"attribute.catInUse.error", name);
		}

		UserAttributeCategory attributeCategory = new UserAttributeCategory();
		attributeCategory.setName(name);
		attributeCategory.setWeight(weight);
		attributeCategory.setRealm(getCurrentRealm());
		
		createResource(attributeCategory, new TransactionAdapter<UserAttributeCategory>() {
			@Override
			public void afterOperation(UserAttributeCategory resource, Map<String,String> properties) {
				registerPropertyCategory(resource);
			}
		});


		return attributeCategory;

	}

	@Override
	public UserAttributeCategory updateAttributeCategory(UserAttributeCategory category, String name,
			int weight) throws ResourceChangeException, AccessDeniedException {
		assertPermission(UserAttributePermission.UPDATE);

		UserAttributeCategory current = attributeCategoryRepository.getResourceByName(name, getCurrentRealm());
		
		if(current!=null && !current.getId().equals(category.getId())) {
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"attribute.nameInUse.error", name);
		}

		category.setName(name);
		category.setWeight(weight);
		
		updateResource(category, new TransactionAdapter<UserAttributeCategory>() {
			@Override
			public void afterOperation(UserAttributeCategory resource, Map<String,String> properties) {
				registerPropertyCategory(resource);
			}
		});

		return category;


	}

	@Override
	public void deleteAttributeCategory(UserAttributeCategory category)
			throws AccessDeniedException, ResourceChangeException {

		assertPermission(UserAttributePermission.DELETE);
		
		for(UserAttribute attr : category.getAttributes()) {
			if(!attr.isDeleted()) {
				throw new ResourceChangeException(RESOURCE_BUNDLE, "error.hasAttributes", category.getName());
			}
		}
		deleteResource(category);

	}
	
	@Override
	public Long getMaximumCategoryWeight() throws AccessDeniedException {
		assertPermission(UserAttributePermission.READ);
		return attributeCategoryRepository.getMaximumCategoryWeight();
	}


	@Override
	protected AbstractResourceRepository<UserAttributeCategory> getRepository() {
		return attributeCategoryRepository;
	}

	@Override
	protected String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public Class<? extends PermissionType> getPermissionType() {
		return UserAttributePermission.class;
	}

	@Override
	protected Class<UserAttributeCategory> getResourceClass() {
		return UserAttributeCategory.class;
	}

	@Override
	protected void fireResourceCreationEvent(UserAttributeCategory resource) {
		eventService.publishEvent(new UserAttributeCategoryCreatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceCreationEvent(UserAttributeCategory resource, Throwable t) {
		eventService.publishEvent(new UserAttributeCategoryCreatedEvent(this, t,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceUpdateEvent(UserAttributeCategory resource) {
		eventService.publishEvent(new UserAttributeCategoryUpdatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceUpdateEvent(UserAttributeCategory resource, Throwable t) {
		eventService.publishEvent(new UserAttributeCategoryUpdatedEvent(this, t,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceDeletionEvent(UserAttributeCategory resource) {
		eventService.publishEvent(new UserAttributeCategoryDeletedEvent(this,
				getCurrentSession(), resource));	
	}

	@Override
	protected void fireResourceDeletionEvent(UserAttributeCategory resource, Throwable t) {
		eventService.publishEvent(new UserAttributeCategoryDeletedEvent(this, t,
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
	public PropertyCategory registerPropertyCategory(UserAttributeCategory c) {
		return registerPropertyCategory(
				"attributeCategory" + String.valueOf(c.getId()),
				"UserAttributes", c.getWeight(), c.isHidden());
	}
	
	PropertyCategory registerPropertyCategory(String resourceKey,
			String bundle, int weight, boolean hidden) {

		PropertyCategory category = new PropertyCategory();
		category.setBundle(bundle);
		category.setCategoryKey(resourceKey);
		category.setDisplayMode("");
		category.setCategoryGroup("userAttribute");
		category.setWeight(weight);
		category.setUserCreated(true);
		category.setFilter("custom");
		category.setHidden(hidden);
		activeCategories.put(category.getCategoryKey(), category);
		return category;
	}

}
