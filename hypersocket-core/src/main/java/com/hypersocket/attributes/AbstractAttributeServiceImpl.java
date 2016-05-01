package com.hypersocket.attributes;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;

import com.hypersocket.attributes.user.events.UserAttributeCreatedEvent;
import com.hypersocket.attributes.user.events.UserAttributeDeletedEvent;
import com.hypersocket.attributes.user.events.UserAttributeUpdatedEvent;
import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.permissions.Role;
import com.hypersocket.properties.AbstractPropertyTemplate;
import com.hypersocket.properties.NameValuePair;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.PropertyOption;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.properties.ResourcePropertyStore;
import com.hypersocket.properties.ResourcePropertyTemplate;
import com.hypersocket.resource.AbstractAssignableResourceRepository;
import com.hypersocket.resource.AbstractAssignableResourceServiceImpl;
import com.hypersocket.resource.AbstractResource;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.TransactionAdapter;
import com.hypersocket.role.events.RoleEvent;

@Service
public abstract class AbstractAttributeServiceImpl<A extends AbstractAttribute<C>, C extends RealmAttributeCategory<A>, R extends AbstractResource> extends AbstractAssignableResourceServiceImpl<A> implements
		AttributeService<A, C>, ApplicationListener<RoleEvent> {

	static Logger log = LoggerFactory.getLogger(AbstractAttributeServiceImpl.class);
	public static final String RESOURCE_BUNDLE = "UserAttributes";

	@Autowired
	protected
	I18NService i18nService;
	
	@Autowired
	protected EventService eventService;

	protected AttributeRepository<A, C> attributeRepository;
	protected AttributeCategoryRepository<C> categoryRepository;
	protected AttributeCategoryService<A, C> categoryService; 

	protected Map<String, PropertyTemplate> propertyTemplates = new HashMap<String, PropertyTemplate>();
	
	private PermissionType readPermission;
	private PermissionType deletePermission;
	private PermissionType createPermission;
	private PermissionType updatePermission;
	private Class<A> resourceClass;
	private Class<?> permissionType;
	private String resourceBundle;
	private String categoryGroup;
	
	public AbstractAttributeServiceImpl(String resourceBundle, Class<A> resourceClass, Class<?> permissionType,
			PermissionType createPermission, PermissionType readPermission, PermissionType updatePermission, PermissionType deletePermission,
			String categoryGroup) {
		super("attribute");
		this.categoryGroup = categoryGroup;
		this.resourceBundle = resourceBundle;
		this.resourceClass = resourceClass;
		this.permissionType = permissionType;
		this.createPermission = createPermission;
		this.readPermission = readPermission;
		this.updatePermission = updatePermission;
		this.deletePermission = deletePermission;
	}
	
	protected void init() {
		
		for(A attr : attributeRepository.allResources()) {
			registerAttribute(attr);
		}
	}


	@Override
	public A updateAttribute(A attribute, String name,
			Long category, String description, String defaultValue, int weight,
			String type, String displayMode, Boolean readOnly, Boolean encrypted,
			String variableName, Set<Role> roles, Collection<NameValuePair> options) throws ResourceChangeException,
			AccessDeniedException {

		assertPermission(updatePermission);

		attribute.setName(name);
		if (!attribute.getOldName().equalsIgnoreCase(name) && attributeRepository.getResourceByName(attribute.getName(), getCurrentRealm()) != null) {
			throw new ResourceChangeException(resourceBundle,
					"attribute.nameInUse.error", name);
		}
		
		A varAttribute = attributeRepository.getAttributeByVariableName(attribute.getVariableName(), getCurrentRealm());
		if (varAttribute != null && !varAttribute.getId().equals(attribute.getId())) {
			throw new ResourceChangeException(resourceBundle,
					"attribute.variableInUse.error", attribute.getVariableName());
		}

		attribute.setCategory(categoryRepository.getResourceById(category));
		attribute.setDescription(description);
		attribute.setDefaultValue(defaultValue);
		attribute.setWeight(weight);
		attribute.setType(AttributeType.valueOf(type));
		attribute.setDisplayMode(displayMode);
		attribute.setReadOnly(readOnly);
		attribute.setEncrypted(encrypted);
		attribute.setVariableName(variableName);
		attribute.getRoles().clear();
		attribute.getRoles().addAll(roles);
		attribute.setOptions(options);
		
		updateResource(attribute, new HashMap<String,String>(), new TransactionAdapter<A>() {
			public void afterOperation(A resource, Map<String,String> properties) {
				registerAttribute(resource);		
			}
		});

		return attribute;

	}

	@Override
	public A createAttribute(String name, Long category,
			String description, String defaultValue, int weight, String type,
			String displayMode, Boolean readOnly, Boolean encrypted, String variableName, Set<Role> roles, Collection<NameValuePair> options)
			throws ResourceCreationException, AccessDeniedException {
		
		A attribute = createNewAttributeInstance();

		assertPermission(createPermission);

		attribute.setName(name);

		if (attributeRepository.getResourceByName(attribute.getName(), getCurrentRealm()) != null) {
			throw new ResourceCreationException(resourceBundle,
					"attribute.nameInUse.error", name);
		}
		
		if (attributeRepository.getAttributeByVariableName(attribute.getVariableName(), getCurrentRealm()) != null) {
			throw new ResourceCreationException(resourceBundle,
					"attribute.variableInUse.error", attribute.getVariableName());
		}

		C cat = categoryRepository
				.getResourceById(category);
		attribute.setCategory(cat);
		attribute.setDescription(description);
		attribute.setDefaultValue(defaultValue);
		attribute.setWeight(weight);
		attribute.setType(AttributeType.valueOf(type));
		
		attribute.setDisplayMode(displayMode);
		attribute.setReadOnly(readOnly);
		attribute.setEncrypted(encrypted);
		attribute.setVariableName(variableName);
		attribute.setOptions(options);
		attribute.getRoles().clear();
		attribute.getRoles().addAll(roles);

		createResource(attribute, new HashMap<String,String>(), new TransactionAdapter<A>() {
			public void afterOperation(A resource, Map<String,String> properties) {
				registerAttribute(resource);		
			}
		});

		return attribute;
	}

	protected abstract A createNewAttributeInstance();

	@SuppressWarnings("unchecked")
	@Override
	public void deleteAttribute(A attribute)
			throws AccessDeniedException, ResourceChangeException {

		assertPermission(deletePermission);
		
		deleteResource(attribute);

	}

	@Override
	public Long getMaximumAttributeWeight(C cat) throws AccessDeniedException {
		assertPermission(readPermission);
		return attributeRepository.getMaximumAttributeWeight(cat);
	}

	@Override
	protected AbstractAssignableResourceRepository<A> getRepository() {
		return attributeRepository;
	}

	@Override
	protected String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public Class<?> getPermissionType() {
		return permissionType;
	}

	@Override
	protected Class<A> getResourceClass() {
		return resourceClass;
	}

	@Override
	protected void fireResourceCreationEvent(A resource) {
		eventService.publishEvent(new UserAttributeCreatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceCreationEvent(A resource, Throwable t) {
		eventService.publishEvent(new UserAttributeCreatedEvent(this, t,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceUpdateEvent(A resource) {
		eventService.publishEvent(new UserAttributeUpdatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceUpdateEvent(A resource, Throwable t) {
		eventService.publishEvent(new UserAttributeUpdatedEvent(this, t,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceDeletionEvent(A resource) {
		eventService.publishEvent(new UserAttributeDeletedEvent(this,
				getCurrentSession(), resource));	
	}

	@Override
	protected void fireResourceDeletionEvent(A resource, Throwable t) {
		eventService.publishEvent(new UserAttributeDeletedEvent(this, t,
				getCurrentSession(), resource));	
	}

	@Override
	public Collection<String> getPropertyNames(AbstractResource resource) {
		Map<String,PropertyTemplate> userTemplates = getAttributeTemplates(checkResource(resource));
		return userTemplates.keySet();
	}

	@Override
	public Collection<String> getVariableNames(AbstractResource resource) {
		Map<String,PropertyTemplate> userTemplates = getAttributeTemplates(checkResource(resource));
		return userTemplates.keySet();
	}
	
	@Override
	public PropertyTemplate getPropertyTemplate(AbstractResource resource,
			String resourceKey) {
		
		Map<String,PropertyTemplate> userTemplates = getAttributeTemplates(checkResource(resource));
		return userTemplates.get(resourceKey);
	}

	@Override
	public Collection<PropertyCategory> getPropertyCategories(
			AbstractResource resource) {
		
		if(resource == null) {
			return new ArrayList<PropertyCategory>();
		}
		
		Map<String,PropertyTemplate> userTemplates = getAttributeTemplates(checkResource(resource));
		Map<Integer,PropertyCategory> results = new HashMap<Integer,PropertyCategory>();
		
		for(PropertyTemplate t : userTemplates.values()) {
			if(!results.containsKey(t.getCategory().getId())) {
				PropertyCategory cat = new PropertyCategory();
				cat.setBundle(t.getCategory().getBundle());
				cat.setCategoryGroup(categoryGroup);
				cat.setCategoryKey(t.getCategory().getCategoryKey());
				cat.setWeight(t.getCategory().getWeight());
				cat.setDisplayMode(t.getCategory().getDisplayMode());
				cat.setUserCreated(true);
				cat.setSystemOnly(t.getCategory().isSystemOnly());
				cat.setHidden(t.getCategory().isHidden());
				cat.setFilter(t.getCategory().getFilter());
				cat.setName(t.getCategory().getName());
				
				results.put(cat.getId(), cat);
			}
			results.get(t.getCategory().getId()).getTemplates().add(new ResourcePropertyTemplate(t, resource));
		}
		
		return results.values();
	}

	@Override
	public Collection<PropertyTemplate> getPropertyTemplates(
			AbstractResource resource) {
		
		Map<String,PropertyTemplate> userTemplates = getAttributeTemplates(checkResource(resource));
		return userTemplates.values();
	}

	@Override
	public boolean hasPropertyTemplate(AbstractResource resource,
			String resourceKey) {

		Map<String,PropertyTemplate> userTemplates = getAttributeTemplates(checkResource(resource));
		return userTemplates.containsKey(resourceKey);

	}
	
	protected abstract R checkResource(AbstractResource resource);
	
	protected abstract Map<String, PropertyTemplate> getAttributeTemplates(R principal);

	protected void registerAttribute(A attr) {

		String resourceKey = "attributeCategory"
				+ String.valueOf(attr.getCategory().getId());
		
		PropertyCategory cat = categoryService.getCategoryByResourceKey(resourceKey);
		if (cat==null) {
			cat = categoryService.registerPropertyCategory(attr.getCategory());
		} 
		
		registerPropertyItem(cat, attr);
	}

	protected void registerPropertyItem(PropertyCategory cat, A attr) {
//		registerPropertyItem(cat, attributeRepository.getDatabasePropertyStore(), attr.getVariableName(),
//				attr.generateMetaData(), "", attr.getWeight(),
//				attr.getHidden(), attr.getDisplayMode(), attr.getReadOnly(), attr.getDefaultValue(),
//				true, attr.getEncrypted(), null);
//	}
//
//	void registerPropertyItem(PropertyCategory category,
//			PropertyStore propertyStore, String resourceKey, String metaData,
//			String mapping, int weight, boolean hidden, String displayMode, boolean readOnly,
//			String defaultValue, boolean isVariable, boolean encrypted,
//			String defaultsToProperty) {

		if (log.isInfoEnabled()) {
			log.info("Registering property " + attr.getVariableName());
		}
		
		String defaultValue =  attr.getDefaultValue();
		if (attr.getDefaultValue() != null && attr.getDefaultValue().startsWith("classpath:")) {
			String url = attr.getDefaultValue().substring(10);
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


		PropertyTemplate template = propertyTemplates.get(attr.getVariableName());
		if (template == null) {
			template = new PropertyTemplate();
			template.setResourceKey(attr.getVariableName());
		}

		template.getAttributes().put("inputType", attr.getType().toString().toLowerCase());
		template.getAttributes().put("filter", "custom");
		
		template.setDefaultValue(defaultValue);
		template.setName(attr.getName());
		template.setDescription(attr.getDescription());
		template.setWeight(attr.getWeight());
		template.setHidden(attr.getHidden());
		template.setDisplayMode(attr.getDisplayMode().equalsIgnoreCase("admin") ? "admin" : "");
		template.setReadOnly(attr.getReadOnly());
		template.setMapping("");
		template.setCategory(cat);
		for(NameValuePair nvp : attr.getOptions()) {
			template.getOptions().add(new PropertyOption(nvp.getValue(), nvp.getName(), nvp.getValue()));
		}
		template.setEncrypted(attr.getEncrypted());
		template.setDefaultsToProperty("");
		template.setPropertyStore(getStore());

		cat.getTemplates().remove(template);
		cat.getTemplates().add(template);

		propertyTemplates.put(attr.getVariableName(), template);

		Collections.sort(cat.getTemplates(),
				new Comparator<AbstractPropertyTemplate>() {
					@Override
					public int compare(AbstractPropertyTemplate cat1,
							AbstractPropertyTemplate cat2) {
						return cat1.getWeight().compareTo(cat2.getWeight());
					}
				});
	}

	protected ResourcePropertyStore getStore() {
		return attributeRepository.getDatabasePropertyStore();
	}

	@Override
	public A getAttributeByVariableName(String attributeName) throws AccessDeniedException {
		assertPermission(readPermission);
		return attributeRepository.getAttributeByVariableName(attributeName, getCurrentRealm());
	}
}
