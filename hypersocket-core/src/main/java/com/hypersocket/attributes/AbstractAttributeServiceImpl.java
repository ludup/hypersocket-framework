package com.hypersocket.attributes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.permissions.Role;
import com.hypersocket.properties.AbstractPropertyTemplate;
import com.hypersocket.properties.NameValuePair;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.PropertyResolver;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.properties.ResourcePropertyStore;
import com.hypersocket.properties.ResourcePropertyTemplate;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.AbstractAssignableResourceServiceImpl;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.SimpleResource;

@Service
public abstract class AbstractAttributeServiceImpl<A extends AbstractAttribute<C>, C extends RealmAttributeCategory<A>, R extends SimpleResource> extends AbstractAssignableResourceServiceImpl<A> implements
		AttributeService<A, C> {

	static Logger log = LoggerFactory.getLogger(AbstractAttributeServiceImpl.class);
	public static final String RESOURCE_BUNDLE = "UserAttributes";

	@Autowired
	protected I18NService i18nService;
	
	@Autowired
	protected EventService eventService;

	@Autowired
	protected RealmService realmService; 
	
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

	private PropertyResolver propertyResolver;
	
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
		
		propertyResolver = createPropertyResolver();
	}
	
	@Override
	public PropertyResolver getPropertyResolver() {
		return propertyResolver;
	}
	
	protected PropertyResolver createPropertyResolver() {
		return new AttributePropertyResolver<A,C,R>(this);
	}
	
	protected void init() {

	}


	@Override
	public A updateAttribute(A attribute, String name,
			Long category, String description, String defaultValue, int weight,
			String type, String displayMode, Boolean readOnly, Boolean required, Boolean encrypted,
			String variableName, Set<Role> roles, Collection<NameValuePair> options) throws AccessDeniedException, ResourceException {

		assertPermission(updatePermission);

		attribute.setName(name);
		if (!attribute.getOldName().equalsIgnoreCase(name) && getRepository().getResourceByName(attribute.getName(), getCurrentRealm()) != null) {
			throw new ResourceChangeException(resourceBundle,
					"attribute.nameInUse.error", name);
		}
		
		A varAttribute = getRepository().getAttributeByVariableName(attribute.getVariableName(), getCurrentRealm());
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
		attribute.setRequired(required);
		attribute.setEncrypted(encrypted);
		attribute.setVariableName(variableName);
		attribute.setOptions(options);
		
		updateResource(attribute, roles, new HashMap<String,String>());

		return attribute;

	}

	@Override
	public A createAttribute(String name, Long category,
			String description, String defaultValue, int weight, String type,
			String displayMode, Boolean readOnly, Boolean required, Boolean encrypted, String variableName, Set<Role> roles, Collection<NameValuePair> options)
			throws ResourceException, AccessDeniedException {
		
		A attribute = createNewAttributeInstance();

		assertPermission(createPermission);

		attribute.setName(name);

		if (getRepository().getResourceByName(attribute.getName(), getCurrentRealm()) != null) {
			throw new ResourceCreationException(resourceBundle,
					"attribute.nameInUse.error", name);
		}
		
		if (getRepository().getAttributeByVariableName(attribute.getVariableName(), getCurrentRealm()) != null) {
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
		attribute.setRequired(required);
		attribute.setEncrypted(encrypted);
		attribute.setVariableName(variableName);
		attribute.setOptions(options);
		attribute.getRoles().clear();
		attribute.getRoles().addAll(roles);

		createResource(attribute, new HashMap<String,String>());

		return attribute;
	}

	protected abstract A createNewAttributeInstance();

	@SuppressWarnings("unchecked")
	@Override
	public void deleteAttribute(A attribute)
			throws AccessDeniedException, ResourceException {

		assertPermission(deletePermission);
		
		deleteResource(attribute);

	}

	@Override
	public Long getMaximumAttributeWeight(C cat) throws AccessDeniedException {
		assertPermission(readPermission);
		return getRepository().getMaximumAttributeWeight(cat);
	}

	protected abstract AttributeRepository<A, C> getRepository();

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
	
	protected abstract R checkResource(SimpleResource resource);
	
	protected abstract Map<String, PropertyTemplate> getAttributeTemplates(R principal);

	protected PropertyTemplate registerAttribute(A attr) {

		String resourceKey = "attributeCategory"
				+ String.valueOf(attr.getCategory().getId());
		
		PropertyCategory cat = categoryService.getCategoryByResourceKey(resourceKey);
		if (cat==null) {
			cat = categoryService.registerPropertyCategory(attr.getCategory());
		} 
		
		return createPropertyTemplate(cat, attr);
	}

	protected PropertyTemplate createPropertyTemplate(PropertyCategory cat, A attr) {

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

		template.getAttributes().put("inputType", attr.getType().getInputType());
		template.getAttributes().put("filter", "custom");
		
		template.setDefaultValue(defaultValue);
		template.setName(attr.getName());
		template.setDescription(attr.getDescription());
		template.setWeight(attr.getWeight());
		template.setHidden(attr.getHidden());
		template.setDisplayMode(attr.getDisplayMode().equalsIgnoreCase("admin") ? "admin" : "");
		template.setReadOnly(attr.getReadOnly());
		template.setRequired(attr.getRequired());
		template.setMapping("");
		template.setCategory(cat);
		if(!attr.getOptions().isEmpty()) {
			StringBuffer buf = new StringBuffer();
			StringBuffer attrBuf = new StringBuffer();
			buf.append("{");
			buf.append("\"");
			buf.append("options");
			buf.append("\": ");
			buf.append("[");
			int pair = 0;
			for(NameValuePair nvp : attr.getOptions()) {
				if(pair > 0) {
					buf.append(",");
					attrBuf.append(",");
				}
				attrBuf.append(nvp.getValue());
				buf.append("{ \"name\": \"");
				buf.append(StringEscapeUtils.escapeEcmaScript(nvp.getName()));
				buf.append("\", \"value\": \"");
				buf.append(StringEscapeUtils.escapeEcmaScript(nvp.getValue()));
				buf.append("\"}");
				pair++;
			}
			buf.append("]");
			buf.append("}");
			template.setMetaData(buf.toString());
			template.getAttributes().put("options", StringEscapeUtils.escapeEcmaScript(attrBuf.toString()));
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
		
		return template;
	}

	protected ResourcePropertyStore getStore() {
		return getRepository().getDatabasePropertyStore();
	}

	@Override
	public A getAttributeByVariableName(String attributeName) throws AccessDeniedException {
		assertPermission(readPermission);
		return getRepository().getAttributeByVariableName(attributeName, getCurrentRealm());
	}
	
	public static class AttributePropertyResolver<A extends AbstractAttribute<C>, C extends RealmAttributeCategory<A>, R extends SimpleResource> implements PropertyResolver {
		
		protected AbstractAttributeServiceImpl<A,C,R> service;

		public AttributePropertyResolver(AbstractAttributeServiceImpl<A,C,R> service) {
			this.service = service;
		}
		
		@Override
		public Collection<String> getPropertyNames(SimpleResource resource) {
			Map<String,PropertyTemplate> userTemplates = service.getAttributeTemplates(service.checkResource(resource));
			return userTemplates.keySet();
		}

		@Override
		public Collection<String> getVariableNames(SimpleResource resource) {
			Map<String,PropertyTemplate> userTemplates = service.getAttributeTemplates(service.checkResource(resource));
			return userTemplates.keySet();
		}
		
		@Override
		public PropertyTemplate getPropertyTemplate(SimpleResource resource,
				String resourceKey) {
			
			Map<String,PropertyTemplate> userTemplates = service.getAttributeTemplates(service.checkResource(resource));
			return userTemplates.get(resourceKey);
		}

		@Override
		public Collection<PropertyCategory> getPropertyCategories(
				SimpleResource resource) {
			
			if(!(resource instanceof Principal)) {
				return Collections.<PropertyCategory>emptyList();
			}
			
			Map<String,PropertyTemplate> userTemplates = service.getAttributeTemplates(service.checkResource(resource));
			Map<Integer,PropertyCategory> results = new HashMap<Integer,PropertyCategory>();
			
			for(PropertyTemplate t : userTemplates.values()) {
				
				if(t == null) {
					log.warn("BUG: NULL property template");
					continue;
				}

				if(t.getCategory() == null) {
					log.warn("BUG: NULL property template");
					continue;
				}
				
				if(!results.containsKey(t.getCategory().getId())) {
					PropertyCategory cat = new PropertyCategory(t.getCategory());
					results.put(cat.getId(), cat);
				}
				results.get(t.getCategory().getId()).getTemplates().add(new ResourcePropertyTemplate(t, resource));
			}
			
			return results.values();
		}

		@Override
		public Collection<PropertyTemplate> getPropertyTemplates(
				SimpleResource resource) {
			
			Map<String,PropertyTemplate> userTemplates = service.getAttributeTemplates(service.checkResource(resource));
			return userTemplates.values();
		}

		@Override
		public boolean hasPropertyTemplate(SimpleResource resource,
				String resourceKey) {

			Map<String,PropertyTemplate> userTemplates = service.getAttributeTemplates(service.checkResource(resource));
			return userTemplates.containsKey(resourceKey);

		}
	}
	
	public String getCategoryGroup() {
		return categoryGroup;
	}

	public void setCategoryGroup(String categoryGroup) {
		this.categoryGroup = categoryGroup;
	}
}
