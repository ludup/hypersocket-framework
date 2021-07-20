package com.hypersocket.attributes.role;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.attributes.AbstractAttributeServiceImpl;
import com.hypersocket.attributes.role.events.RoleAttributeCreatedEvent;
import com.hypersocket.attributes.role.events.RoleAttributeDeletedEvent;
import com.hypersocket.attributes.role.events.RoleAttributeUpdatedEvent;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.Role;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmAdapter;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.SimpleResource;

@Service
public class RoleAttributeServiceImpl extends AbstractAttributeServiceImpl<RoleAttribute, RoleAttributeCategory, Role>
		implements RoleAttributeService {

	static Logger log = LoggerFactory.getLogger(RoleAttributeServiceImpl.class);

	@Autowired
	private RoleAttributeRepository userAttributeRepository;

	@Autowired
	private RoleAttributeCategoryRepository userAttributeCategoryRepository;

	@Autowired
	private RoleAttributeCategoryService userAttributeCategoryService;
	
	public RoleAttributeServiceImpl() {
		super(PermissionService.RESOURCE_BUNDLE, RoleAttribute.class, RoleAttributePermission.class, RoleAttributePermission.CREATE,
				RoleAttributePermission.READ, RoleAttributePermission.UPDATE, RoleAttributePermission.DELETE, "RoleAttributes");
	}

	@PostConstruct
	protected void init() {
		
		categoryRepository = userAttributeCategoryRepository;
		categoryService = userAttributeCategoryService;

		PermissionCategory cat = permissionService.registerPermissionCategory(PermissionService.RESOURCE_BUNDLE,
				"category.RoleAttributes");

		for (RoleAttributePermission p : RoleAttributePermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		eventService.registerEvent(RoleAttributeCreatedEvent.class, PermissionService.RESOURCE_BUNDLE);
		eventService.registerEvent(RoleAttributeUpdatedEvent.class, PermissionService.RESOURCE_BUNDLE);
		eventService.registerEvent(RoleAttributeDeletedEvent.class, PermissionService.RESOURCE_BUNDLE);

		realmService.registerRealmListener(new RealmAdapter() {
			@Override
			public void onDeleteRealm(Realm realm) throws ResourceException, AccessDeniedException {
				getRepository().deleteRealm(realm);
				userAttributeCategoryRepository.deleteRealm(realm);
			}	
		});
		
		super.init();
	}
	
	@Override
	protected RoleAttributeRepository getRepository() {
		return userAttributeRepository;
	}

	@Override
	protected RoleAttribute createNewAttributeInstance() {
		return new RoleAttribute();
	}

	@Override
	protected Role checkResource(SimpleResource resource) {
		if(resource==null) {
			return null;
		}
		if(!(resource instanceof Role)) {
			throw new IllegalArgumentException("Resource must be a Role in order to get attributes");
		}
		return (Role) resource;
	}

	protected Map<String, PropertyTemplate> getAttributeTemplates(Role role) {

		Collection<RoleAttribute> attributes = getRepository().getResources(getCurrentRealm());
		Map<String, PropertyTemplate> results = new HashMap<String, PropertyTemplate>();

		for (RoleAttribute attr : attributes) {
			if(!propertyTemplates.containsKey(attr.getVariableName())) {
				propertyTemplates.put(attr.getVariableName(), registerAttribute(attr));
			}
			results.put(attr.getVariableName(), propertyTemplates.get(attr.getVariableName()));
		}

		return results;

	}
	
//	public void registerPropertyItem(PropertyCategory cat, RoleAttribute attr) {
//
//		if (log.isInfoEnabled()) {
//			log.info("Registering property " + attr.getVariableName());
//		}
//		
//		String defaultValue =  attr.getDefaultValue();
//		if (attr.getDefaultValue() != null && attr.getDefaultValue().startsWith("classpath:")) {
//			String url = attr.getDefaultValue().substring(10);
//			InputStream in = getClass().getResourceAsStream(url);
//			try {
//				if (in != null) {
//					try {
//						defaultValue = IOUtils.toString(in);
//					} catch (IOException e) {
//						log.error(
//								"Failed to load default value classpath resource "
//										+ defaultValue, e);
//					}
//				} else {
//					log.error("Failed to load default value classpath resource "
//							+ url);
//				}
//			} finally {
//				IOUtils.closeQuietly(in);
//			}
//		}
//
//
//		PropertyTemplate template = propertyTemplates.get(attr.getVariableName());
//		if (template == null) {
//			template = new PropertyTemplate();
//			template.setResourceKey(attr.getVariableName());
//		}
//
//		template.getAttributes().put("inputType", attr.getType().getInputType());
//		template.getAttributes().put("filter", "custom");
//		
//		template.setName(attr.getName());
//		template.setDescription(attr.getDescription());
//		template.setDefaultValue(defaultValue);
//		template.setWeight(attr.getWeight());
//		template.setHidden(attr.getHidden());
//		template.setDisplayMode(attr.getDisplayMode().equalsIgnoreCase("admin") ? "admin" : "");
//		template.setReadOnly(attr.getReadOnly());
//		template.setMapping("");
//		template.setCategory(cat);
//		template.setEncrypted(attr.getEncrypted());
//		template.setDefaultsToProperty("");
//		template.setPropertyStore(userAttributeRepository.getDatabasePropertyStore());
//
//		cat.getTemplates().remove(template);
//		cat.getTemplates().add(template);
//
//		propertyTemplates.put(attr.getVariableName(), template);
//
//		Collections.sort(cat.getTemplates(),
//				new Comparator<AbstractPropertyTemplate>() {
//					@Override
//					public int compare(AbstractPropertyTemplate cat1,
//							AbstractPropertyTemplate cat2) {
//						return cat1.getWeight().compareTo(cat2.getWeight());
//					}
//				});
//
//
//		synchronized (userPropertyTemplates) {
//			userPropertyTemplates.clear();	
//		}
//	}
	
	@Override
	protected void fireResourceCreationEvent(RoleAttribute resource) {
		eventService.publishEvent(new RoleAttributeCreatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceCreationEvent(RoleAttribute resource, Throwable t) {
		eventService.publishEvent(new RoleAttributeCreatedEvent(this, t,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceUpdateEvent(RoleAttribute resource) {
		eventService.publishEvent(new RoleAttributeUpdatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceUpdateEvent(RoleAttribute resource, Throwable t) {
		eventService.publishEvent(new RoleAttributeUpdatedEvent(this, t,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceDeletionEvent(RoleAttribute resource) {
		eventService.publishEvent(new RoleAttributeDeletedEvent(this,
				getCurrentSession(), resource));	
	}

	@Override
	protected void fireResourceDeletionEvent(RoleAttribute resource, Throwable t) {
		eventService.publishEvent(new RoleAttributeDeletedEvent(this, t,
				getCurrentSession(), resource));	
	}
}
