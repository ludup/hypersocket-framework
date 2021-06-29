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

	A updateAttribute(A attribute, String name,
			Long category, String description, String defaultValue, int weight,
			String type, String displayMode, Boolean readOnly, Boolean required, Boolean encrypted,
			String variableName, Set<Role> roles, Collection<NameValuePair> options) throws AccessDeniedException, ResourceException;

	A createAttribute(String name, Long category,
			String description, String defaultValue, int weight, String type,
			String displayMode, Boolean readOnly, Boolean required, Boolean encrypted, String variableName, Set<Role> roles, Collection<NameValuePair> options)
			throws AccessDeniedException, ResourceException;

	void deleteAttribute(A attribute)
			throws AccessDeniedException, ResourceException;

	Long getMaximumAttributeWeight(C cat) throws AccessDeniedException;

	A getAttributeByVariableName(String attributeName) throws AccessDeniedException;

	PropertyResolver getPropertyResolver();


}
