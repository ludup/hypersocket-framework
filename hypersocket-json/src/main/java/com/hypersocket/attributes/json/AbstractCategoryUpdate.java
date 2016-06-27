package com.hypersocket.attributes.json;

public class AbstractCategoryUpdate {

	protected Long id;
	protected String name;
	protected String context;
	protected int weight;

	public AbstractCategoryUpdate() {
		super();
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