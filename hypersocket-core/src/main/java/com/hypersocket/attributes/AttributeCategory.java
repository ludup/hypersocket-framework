package com.hypersocket.attributes;

public interface AttributeCategory<T extends AbstractAttribute<?>> {

	Long getId();

	String getName();
	
	String getCategoryNamespace();

	Integer getWeight();
	
	String getVisibilityDependsOn();
	
	String getVisibilityDependsValue();
}
