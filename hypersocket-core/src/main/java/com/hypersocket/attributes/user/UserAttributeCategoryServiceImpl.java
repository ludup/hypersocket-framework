package com.hypersocket.attributes.user;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.attributes.AbstractAttributeCategoryServiceImpl;
import com.hypersocket.properties.PropertyCategory;

@Service
public class UserAttributeCategoryServiceImpl extends AbstractAttributeCategoryServiceImpl<UserAttribute, UserAttributeCategory>
		implements UserAttributeCategoryService {

	public static final String RESOURCE_BUNDLE = "UserAttributes";

	@Autowired
	private UserAttributeCategoryRepository userAttributeCategoryRepository;

	public UserAttributeCategoryServiceImpl() {
		super(RESOURCE_BUNDLE, UserAttributePermission.class, UserAttributeCategory.class,
				UserAttributePermission.CREATE, UserAttributePermission.READ, UserAttributePermission.UPDATE,
				UserAttributePermission.DELETE);
	} 

	@PostConstruct
	protected void init() {
		attributeCategoryRepository = userAttributeCategoryRepository;
		super.init();
	}

	@Override
	protected UserAttributeCategory newAttributeCategoryInstance() {
		return new UserAttributeCategory();
	}


	@Override
	protected PropertyCategory registerPropertyCategory(String resourceKey, String categoryNamespace, String bundle, int weight, boolean hidden, String name, String visibilityDependsOn,
			String visibilityDependsValue) {
		final PropertyCategory cat = super.registerPropertyCategory(resourceKey, categoryNamespace, bundle, weight, hidden, name, visibilityDependsOn, visibilityDependsValue);
		cat.setCategoryGroup("userAttribute");
		return cat;
	}
}
