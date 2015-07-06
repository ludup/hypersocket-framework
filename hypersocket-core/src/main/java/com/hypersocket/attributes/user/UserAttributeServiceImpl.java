package com.hypersocket.attributes.user;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.attributes.user.events.UserAttributeCreatedEvent;
import com.hypersocket.attributes.user.events.UserAttributeDeletedEvent;
import com.hypersocket.attributes.user.events.UserAttributeEvent;
import com.hypersocket.attributes.user.events.UserAttributeUpdatedEvent;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.Role;
import com.hypersocket.properties.AbstractPropertyTemplate;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.PropertyStore;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.realm.Principal;
import com.hypersocket.resource.AbstractAssignableResourceRepository;
import com.hypersocket.resource.AbstractAssignableResourceServiceImpl;
import com.hypersocket.resource.AbstractResource;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.TransactionAdapter;

@Service
public class UserAttributeServiceImpl extends AbstractAssignableResourceServiceImpl<UserAttribute> implements
		UserAttributeService {

	static Logger log = LoggerFactory.getLogger(UserAttributeServiceImpl.class);
	public static final String RESOURCE_BUNDLE = "UserAttributes";

	@Autowired
	I18NService i18nService;

	@Autowired
	UserAttributeRepository attributeRepository;

	@Autowired
	UserAttributeCategoryRepository categoryRepository;

	@Autowired
	UserAttributeCategoryService categoryService; 
	
	@Autowired
	EventService eventService;

	Map<String, PropertyTemplate> propertyTemplates = new HashMap<String, PropertyTemplate>();
	
	Map<Principal,Map<String,PropertyTemplate>> userPropertyTemplates = new HashMap<Principal,Map<String,PropertyTemplate>>();
	
	public UserAttributeServiceImpl() {
		super("attribute");
	}
	
	@PostConstruct
	private void postConstruct() {

		PermissionCategory cat = permissionService.registerPermissionCategory(
				RESOURCE_BUNDLE, "category.userAttributes");

		for (UserAttributePermission p : UserAttributePermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		i18nService.registerBundle(RESOURCE_BUNDLE);

		eventService.registerEvent(UserAttributeEvent.class, RESOURCE_BUNDLE);
		eventService
				.registerEvent(UserAttributeCreatedEvent.class, RESOURCE_BUNDLE);
		eventService
				.registerEvent(UserAttributeUpdatedEvent.class, RESOURCE_BUNDLE);
		eventService
				.registerEvent(UserAttributeDeletedEvent.class, RESOURCE_BUNDLE);

		
		for(UserAttribute attr : attributeRepository.allResources()) {
			registerAttribute(attr);
		}
	}


	@Override
	public UserAttribute updateAttribute(UserAttribute attribute, String name,
			Long category, String description, String defaultValue, int weight,
			String type, String displayMode, Boolean readOnly, Boolean encrypted,
			String variableName, Set<Role> roles) throws ResourceChangeException,
			AccessDeniedException {

		assertPermission(UserAttributePermission.UPDATE);

		attribute.setName(name);
		if (!attribute.getOldName().equals(name) && attributeRepository.getResourceByName(attribute.getName(), getCurrentRealm()) != null) {
			throw new ResourceChangeException(RESOURCE_BUNDLE,
					"attribute.nameInUse.error", name);
		}

		attribute.setCategory(categoryRepository.getResourceById(category));
		attribute.setDescription(description);
		attribute.setDefaultValue(defaultValue);
		attribute.setWeight(weight);
		if (type.equals("TEXT")) {
			attribute.setType(UserAttributeType.TEXT);
		} else {
			attribute.setType(UserAttributeType.PASSWORD);
		}

		attribute.setDisplayMode(displayMode);
		attribute.setReadOnly(readOnly);
		attribute.setEncrypted(encrypted);
		attribute.setVariableName(variableName);
		attribute.getRoles().clear();
		attribute.getRoles().addAll(roles);
		
		updateResource(attribute, new HashMap<String,String>(), new TransactionAdapter<UserAttribute>() {
			public void afterOperation(UserAttribute resource, Map<String,String> properties) {
				registerAttribute(resource);		
			}
		});

		return attribute;

	}

	@Override
	public UserAttribute createAttribute(String name, Long category,
			String description, String defaultValue, int weight, String type,
			String displayMode, Boolean readOnly, Boolean encrypted, String variableName, Set<Role> roles)
			throws ResourceCreationException, AccessDeniedException {
		
		UserAttribute attribute = new UserAttribute();

		assertPermission(UserAttributePermission.CREATE);

		attribute.setName(name);

		if (attributeRepository.getResourceByName(attribute.getName(), getCurrentRealm()) != null) {
			throw new ResourceCreationException(RESOURCE_BUNDLE,
					"attribute.nameInUse.error", name);
		}

		UserAttributeCategory cat = categoryRepository
				.getResourceById(category);
		attribute.setCategory(cat);
		attribute.setDescription(description);
		attribute.setDefaultValue(defaultValue);
		attribute.setWeight(weight);
		attribute.setType(UserAttributeType.valueOf(type));
		
		attribute.setDisplayMode(displayMode);
		attribute.setReadOnly(readOnly);
		attribute.setEncrypted(encrypted);
		attribute.setVariableName(variableName);
		attribute.getRoles().clear();
		attribute.getRoles().addAll(roles);

		createResource(attribute, new HashMap<String,String>(), new TransactionAdapter<UserAttribute>() {
			public void afterOperation(UserAttribute resource, Map<String,String> properties) {
				registerAttribute(resource);		
			}
		});

		return attribute;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deleteAttribute(UserAttribute attribute)
			throws AccessDeniedException, ResourceChangeException {

		assertPermission(UserAttributePermission.DELETE);
		
		deleteResource(attribute);

	}

	@Override
	public Long getMaximumAttributeWeight(UserAttributeCategory cat) throws AccessDeniedException {
		assertPermission(UserAttributePermission.READ);
		return attributeRepository.getMaximumAttributeWeight(cat);
	}

	@Override
	protected AbstractAssignableResourceRepository<UserAttribute> getRepository() {
		return attributeRepository;
	}

	@Override
	protected String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public Class<?> getPermissionType() {
		return UserAttributePermission.class;
	}

	@Override
	protected Class<UserAttribute> getResourceClass() {
		return UserAttribute.class;
	}

	@Override
	protected void fireResourceCreationEvent(UserAttribute resource) {
		eventService.publishEvent(new UserAttributeCreatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceCreationEvent(UserAttribute resource, Throwable t) {
		eventService.publishEvent(new UserAttributeCreatedEvent(this, t,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceUpdateEvent(UserAttribute resource) {
		eventService.publishEvent(new UserAttributeUpdatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceUpdateEvent(UserAttribute resource, Throwable t) {
		eventService.publishEvent(new UserAttributeUpdatedEvent(this, t,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceDeletionEvent(UserAttribute resource) {
		eventService.publishEvent(new UserAttributeDeletedEvent(this,
				getCurrentSession(), resource));	
	}

	@Override
	protected void fireResourceDeletionEvent(UserAttribute resource, Throwable t) {
		eventService.publishEvent(new UserAttributeDeletedEvent(this, t,
				getCurrentSession(), resource));	
	}

	@Override
	public Collection<String> getPropertyNames(AbstractResource resource) {
		Map<String,PropertyTemplate> userTemplates = getUserTemplates(checkResource(resource));
		return userTemplates.keySet();
	}

	@Override
	public Collection<String> getVariableNames(AbstractResource resource) {
		Map<String,PropertyTemplate> userTemplates = getUserTemplates(checkResource(resource));
		return userTemplates.keySet();
	}
	
	@Override
	public PropertyTemplate getPropertyTemplate(AbstractResource resource,
			String resourceKey) {
		
		Map<String,PropertyTemplate> userTemplates = getUserTemplates(checkResource(resource));
		return userTemplates.get(resourceKey);
	}

	@Override
	public Collection<PropertyCategory> getPropertyCategories(
			AbstractResource resource) {
		
		if(resource == null) {
			return new ArrayList<PropertyCategory>();
		}
		Map<String,PropertyTemplate> userTemplates = getUserTemplates(checkResource(resource));
		Set<PropertyCategory> results = new HashSet<PropertyCategory>();
		
		for(PropertyTemplate t : userTemplates.values()) {
			results.add(t.getCategory());
		}
		
		return results;
	}

	@Override
	public Collection<PropertyTemplate> getPropertyTemplates(
			AbstractResource resource) {
		
		Map<String,PropertyTemplate> userTemplates = getUserTemplates(checkResource(resource));
		return userTemplates.values();
	}

	@Override
	public boolean hasPropertyTemplate(AbstractResource resource,
			String resourceKey) {

		Map<String,PropertyTemplate> userTemplates = getUserTemplates(checkResource(resource));
		return userTemplates.containsKey(resourceKey);

	}
	
	
	private Principal checkResource(AbstractResource resource) {
		
		if(resource==null) {
			return null;
		}
		
		if(!(resource instanceof Principal)) {
			throw new IllegalArgumentException("Resource must be a Principal in order to get UserAttributes");
		}
		
		return (Principal) resource;
	}
	
	Map<String, PropertyTemplate> getUserTemplates(Principal principal)  {
		
		if(principal == null) {
			return new HashMap<String, PropertyTemplate>();
		}
		
		synchronized (userPropertyTemplates) {
			
			if(userPropertyTemplates.containsKey(principal)) {
				return userPropertyTemplates.get(principal);
			}
			
			Collection<UserAttribute> attributes = getPersonalResources(principal);
			Map<String, PropertyTemplate> results = new HashMap<String, PropertyTemplate>();
			
			for(UserAttribute attr : attributes) {
				results.put(attr.getVariableName(), propertyTemplates.get(attr.getVariableName()));
			}
			
			userPropertyTemplates.put(principal, results);
			return results;
		}
		
	}

	void registerAttribute(UserAttribute attr) {

		String resourceKey = "attributeCategory"
				+ String.valueOf(attr.getCategory().getId());
		
		PropertyCategory cat = categoryService.getCategoryByResourceKey(resourceKey);
		if (cat==null) {
			cat = categoryService.registerPropertyCategory(attr.getCategory());
		} 
		
		registerPropertyItem(cat, attr);
	}

	void registerPropertyItem(PropertyCategory cat, UserAttribute attr) {
		registerPropertyItem(cat, attributeRepository.getDatabasePropertyStore(), attr.getVariableName(),
				attr.generateMetaData(), "", attr.getWeight(),
				attr.getHidden(), attr.getDisplayMode(), attr.getReadOnly(), attr.getDefaultValue(),
				true, attr.getEncrypted(), null);
	}

	void registerPropertyItem(PropertyCategory category,
			PropertyStore propertyStore, String resourceKey, String metaData,
			String mapping, int weight, boolean hidden, String displayMode, boolean readOnly,
			String defaultValue, boolean isVariable, boolean encrypted,
			String defaultsToProperty) {

		if (log.isInfoEnabled()) {
			log.info("Registering property " + resourceKey);
		}
		
		if (defaultValue != null && defaultValue.startsWith("classpath:")) {
			String url = defaultValue.substring(10);
			InputStream in = getClass().getResourceAsStream(url);
			try {
				if (in != null) {
					try {
						defaultValue = IOUtils.toString(in);
					} catch (IOException e) {
						log.error(
								"Failed to load default value classpath resource "
										+ defaultValue, e);
					}
				} else {
					log.error("Failed to load default value classpath resource "
							+ url);
				}
			} finally {
				IOUtils.closeQuietly(in);
			}
		}


		PropertyTemplate template = propertyTemplates.get(resourceKey);
		if (template == null) {
			template = new PropertyTemplate();
			template.setResourceKey(resourceKey);
		}

		template.setMetaData(metaData);
		template.setDefaultValue(defaultValue);
		template.setWeight(weight);
		template.setHidden(hidden);
		template.setDisplayMode(displayMode);
		template.setReadOnly(readOnly);
		template.setMapping(mapping);
		template.setCategory(category);
		template.setEncrypted(encrypted);
		template.setDefaultsToProperty(defaultsToProperty);
		template.setPropertyStore(propertyStore);

		category.getTemplates().remove(template);
		category.getTemplates().add(template);

		propertyTemplates.put(resourceKey, template);

		Collections.sort(category.getTemplates(),
				new Comparator<AbstractPropertyTemplate>() {
					@Override
					public int compare(AbstractPropertyTemplate cat1,
							AbstractPropertyTemplate cat2) {
						return cat1.getWeight().compareTo(cat2.getWeight());
					}
				});

		synchronized (userPropertyTemplates) {
			userPropertyTemplates.clear();	
		}
	}

	@Override
	public UserAttribute getAttributeByVariableName(String attributeName) throws AccessDeniedException {
		
		assertPermission(UserAttributePermission.READ);
		
		return attributeRepository.getAttributeByVariableName(attributeName, getCurrentRealm());
	}
}
