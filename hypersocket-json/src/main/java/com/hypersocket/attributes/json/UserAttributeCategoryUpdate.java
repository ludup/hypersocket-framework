package com.hypersocket.attributes.json;

public class UserAttributeCategoryUpdate extends AbstractCategoryUpdate {
	public UserAttributeCategoryUpdate() {

	}

	public UserAttributeCategoryUpdate(Long id, String name, String context,
			int weight) {
		this.id = id;
		this.name = name;
		this.context = context;
		this.weight = weight;
	}
}
