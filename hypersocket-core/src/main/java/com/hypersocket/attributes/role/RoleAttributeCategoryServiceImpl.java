package com.hypersocket.attributes.role;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.attributes.AbstractAttributeCategoryServiceImpl;
import com.hypersocket.properties.PropertyCategory;

@Service
public class RoleAttributeCategoryServiceImpl extends AbstractAttributeCategoryServiceImpl<RoleAttribute, RoleAttributeCategory>
		implements RoleAttributeCategoryService {

	public static final String RESOURCE_BUNDLE = "RoleAttributes";

	@Autowired
	RoleAttributeCategoryRepository userAttributeCategoryRepository;

	public RoleAttributeCategoryServiceImpl() {
		super(RESOURCE_BUNDLE, RoleAttributePermission.class, RoleAttributeCategory.class,
				RoleAttributePermission.CREATE, RoleAttributePermission.READ, RoleAttributePermission.UPDATE,
				RoleAttributePermission.DELETE);
	} 

	@PostConstruct
	protected void init() {
		attributeCategoryRepository = userAttributeCategoryRepository;
		super.init();
	}

	@Override
	protected RoleAttributeCategory newAttributeCategoryInstance() {
		return new RoleAttributeCategory();
	}


	@Override
	protected PropertyCategory registerPropertyCategory(String resourceKey, String bundle, int weight, boolean hidden, String name) {
		final PropertyCategory cat = super.registerPropertyCategory(resourceKey, bundle, weight, hidden, name);
		cat.setCategoryGroup("userAttribute");
		return cat;
	}
}
