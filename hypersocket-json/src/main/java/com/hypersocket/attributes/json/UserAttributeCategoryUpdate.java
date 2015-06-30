package com.hypersocket.attributes.json;

public class UserAttributeCategoryUpdate {

	private Long id;
	private String name;
	private String context;
	private int weight;

	public UserAttributeCategoryUpdate() {

	}

	public UserAttributeCategoryUpdate(Long id, String name, String context,
			int weight) {
		this.id = id;
		this.name = name;
		this.context = context;
		this.weight = weight;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

}
