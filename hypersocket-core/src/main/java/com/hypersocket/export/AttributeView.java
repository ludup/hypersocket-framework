package com.hypersocket.export;

public class AttributeView implements Comparable<AttributeView>{

	String name;

	public AttributeView() {

	}

	public AttributeView(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int compareTo(AttributeView attributeView) {
		return name.compareTo(attributeView.getName());
	}
}
