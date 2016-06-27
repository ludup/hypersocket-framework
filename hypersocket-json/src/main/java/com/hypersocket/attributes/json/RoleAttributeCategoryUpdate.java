package com.hypersocket.attributes.json;

public class RoleAttributeCategoryUpdate extends AbstractCategoryUpdate {

	public RoleAttributeCategoryUpdate() {

	}

	public RoleAttributeCategoryUpdate(Long id, String name, String context,
			int weight) {
		this.id = id;
		this.name = name;
		this.context = context;
		this.weight = weight;
	}

}
