package com.hypersocket.attributes;

public interface AttributeCategory<T extends AbstractAttribute<?>> {

	Long getId();

	String getName();

	Integer getWeight();
	
	String getVisibilityDependsOn();
	
	String getVisibilityDependsValue();
}
