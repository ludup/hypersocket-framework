package com.hypersocket.attributes;

import java.util.List;

import com.hypersocket.resource.AbstractResourceRepository;

public interface AttributeCategoryRepository<C extends RealmAttributeCategory<?>> extends
		AbstractResourceRepository<C> {

	Long getMaximumCategoryWeight();

	List<C> allResources();

}
