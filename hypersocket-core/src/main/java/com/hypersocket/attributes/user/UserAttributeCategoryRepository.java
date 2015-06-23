package com.hypersocket.attributes.user;

import com.hypersocket.resource.AbstractResourceRepository;

public interface UserAttributeCategoryRepository extends
		AbstractResourceRepository<UserAttributeCategory> {

	Long getMaximumCategoryWeight();

}
