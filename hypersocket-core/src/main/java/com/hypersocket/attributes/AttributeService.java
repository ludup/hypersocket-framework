package com.hypersocket.attributes;

import java.util.Collection;
import java.util.Set;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.Role;
import com.hypersocket.properties.NameValuePair;
import com.hypersocket.properties.PropertyResolver;
import com.hypersocket.resource.AbstractAssignableResourceService;
import com.hypersocket.resource.ResourceException;

public interface AttributeService<A extends AbstractAttribute<?>, C extends RealmAttributeCategory<?>> extends AbstractAssignableResourceService<A> {

	public A updateAttribute(A attribute, String name,
			Long category, String description, String defaultValue, int weight,
			String type, String displayMode, Boolean readOnly, Boolean encrypted,
			String variableName, Set<Role> roles, Collection<NameValuePair> options) throws AccessDeniedException, ResourceException;

	public A createAttribute(String name, Long category,
			String description, String defaultValue, int weight, String type,
			String displayMode, Boolean readOnly, Boolean encrypted, String variableName, Set<Role> roles, Collection<NameValuePair> options)
			throws AccessDeniedException, ResourceException;

	public void deleteAttribute(A attribute)
			throws AccessDeniedException, ResourceException;

	public Long getMaximumAttributeWeight(C cat) throws AccessDeniedException;

	public A getAttributeByVariableName(String attributeName) throws AccessDeniedException;

	PropertyResolver getPropertyResolver();


}
