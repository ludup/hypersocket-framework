package com.hypersocket.attributes.user;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.attributes.AbstractAttributeServiceImpl;
import com.hypersocket.attributes.user.events.UserAttributeCreatedEvent;
import com.hypersocket.attributes.user.events.UserAttributeDeletedEvent;
import com.hypersocket.attributes.user.events.UserAttributeEvent;
import com.hypersocket.attributes.user.events.UserAttributeUpdatedEvent;
import com.hypersocket.auth.FakePrincipal;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmAdapter;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.SimpleResource;

@Service
public class UserAttributeServiceImpl extends AbstractAttributeServiceImpl<UserAttribute, UserAttributeCategory, Principal>
		implements UserAttributeService {

	static Logger log = LoggerFactory.getLogger(UserAttributeServiceImpl.class);
	public static final String RESOURCE_BUNDLE = "UserAttributes";

	@Autowired
	private UserAttributeRepository userAttributeRepository;

	@Autowired
	private UserAttributeCategoryRepository userAttributeCategoryRepository;

	@Autowired
	private UserAttributeCategoryService userAttributeCategoryService;

	FakePrincipal allUsersPrincial = new FakePrincipal("allusers");

	public UserAttributeServiceImpl() {
		super(RESOURCE_BUNDLE, UserAttribute.class, UserAttributePermission.class, UserAttributePermission.CREATE,
				UserAttributePermission.READ, UserAttributePermission.UPDATE, UserAttributePermission.DELETE, "userAttributes");
	}

	@PostConstruct
	protected void init() {
		categoryRepository = userAttributeCategoryRepository;
		categoryService = userAttributeCategoryService;

		PermissionCategory cat = permissionService.registerPermissionCategory(RESOURCE_BUNDLE,
				"category.userAttributes");

		for (UserAttributePermission p : UserAttributePermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		i18nService.registerBundle(RESOURCE_BUNDLE);

		eventService.registerEvent(UserAttributeEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(UserAttributeCreatedEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(UserAttributeUpdatedEvent.class, RESOURCE_BUNDLE);
		eventService.registerEvent(UserAttributeDeletedEvent.class, RESOURCE_BUNDLE);
		
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
	protected UserAttributeRepository getRepository() {
		return userAttributeRepository;
	}
	
	@Override
	protected UserAttribute createNewAttributeInstance() {
		return new UserAttribute();
	}

	@Override
	protected Principal checkResource(SimpleResource resource) {
		if(resource==null) {
			return null;
		}
		if(!(resource instanceof Principal)) {
			throw new IllegalArgumentException("Resource must be a Principal in order to get attributes");
		}
		return (Principal) resource;
	}

	protected Map<String, PropertyTemplate> getAttributeTemplates(Principal principal) {

		if (principal == null) {
			principal = allUsersPrincial;
		}

		Collection<UserAttribute> attributes;

		if(principal.equals(allUsersPrincial)) {
			attributes = getRepository().getResources(principal.getRealm());
		} else {
			attributes = getPersonalResources(principal);
		}

		Map<String, PropertyTemplate> results = new HashMap<String, PropertyTemplate>();

		for (UserAttribute attr : attributes) {
			if(!propertyTemplates.containsKey(attr.getVariableName())) {
				propertyTemplates.put(attr.getVariableName(), registerAttribute(attr));
			}
			results.put(attr.getVariableName(), propertyTemplates.get(attr.getVariableName()));
		}

		return results;

	}

//	public void registerPropertyItem(PropertyCategory cat, UserAttribute attr) {
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
//		template.setName(attr.getName());
//		template.setDescription(attr.getDescription());
//		
//		template.getAttributes().put("inputType", attr.getType().getInputType());
//		template.getAttributes().put("filter", "custom");
//		template.getAttributes().put("userAttribute", "true");
//		
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

}
