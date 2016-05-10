package com.hypersocket.attributes.user;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.attributes.AbstractAttribute;
import com.hypersocket.attributes.AbstractAttributeServiceImpl;
import com.hypersocket.attributes.user.events.UserAttributeCreatedEvent;
import com.hypersocket.attributes.user.events.UserAttributeDeletedEvent;
import com.hypersocket.attributes.user.events.UserAttributeEvent;
import com.hypersocket.attributes.user.events.UserAttributeUpdatedEvent;
import com.hypersocket.auth.FakePrincipal;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.realm.Principal;
import com.hypersocket.resource.AbstractResource;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.TransactionOperation;
import com.hypersocket.role.events.RoleEvent;

@Service
public class UserAttributeServiceImpl extends AbstractAttributeServiceImpl<UserAttribute, UserAttributeCategory, Principal>
		implements UserAttributeService {

	static Logger log = LoggerFactory.getLogger(UserAttributeServiceImpl.class);
	public static final String RESOURCE_BUNDLE = "UserAttributes";

	@Autowired
	UserAttributeRepository userAttributeRepository;

	@Autowired
	UserAttributeCategoryRepository userAttributeCategoryRepository;

	@Autowired
	UserAttributeCategoryService userAttributeCategoryService;

	Map<Principal, Map<String, PropertyTemplate>> userPropertyTemplates = new HashMap<Principal, Map<String, PropertyTemplate>>();
	FakePrincipal allUsersPrincial = new FakePrincipal("allusers");

	public UserAttributeServiceImpl() {
		super(RESOURCE_BUNDLE, UserAttribute.class, UserAttributePermission.class, UserAttributePermission.CREATE,
				UserAttributePermission.READ, UserAttributePermission.UPDATE, UserAttributePermission.DELETE, "userAttributes");
	}

	@PostConstruct
	protected void init() {
		attributeRepository = userAttributeRepository;
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

		super.init();
	}

	@Override
	protected UserAttribute createNewAttributeInstance() {
		return new UserAttribute();
	}

	@Override
	protected Principal checkResource(AbstractResource resource) {
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

		synchronized (userPropertyTemplates) {

			if (userPropertyTemplates.containsKey(principal)) {
				return userPropertyTemplates.get(principal);
			}

			Collection<UserAttribute> attributes;
			if (principal.equals(allUsersPrincial)) {
				attributes = getRepository().getResources(principal.getRealm());
			} else {
				attributes = getPersonalResources(principal);
			}

			Map<String, PropertyTemplate> results = new HashMap<String, PropertyTemplate>();

			for (UserAttribute attr : attributes) {
				results.put(attr.getVariableName(), propertyTemplates.get(attr.getVariableName()));
			}

			userPropertyTemplates.put(principal, results);
			return results;
		}

	}
	protected void registerPropertyItem(PropertyCategory cat, UserAttribute attr) {
		super.registerPropertyItem(cat, attr);

		synchronized (userPropertyTemplates) {
			userPropertyTemplates.clear();	
		}
	}

	@Override
	public void onApplicationEvent(RoleEvent event) {
		/**
		 * Really quick hack. We will do better.
		 */
		userPropertyTemplates.clear();
	}
}
