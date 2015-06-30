package com.hypersocket.attributes.user;

import java.util.Set;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.Role;
import com.hypersocket.properties.PropertyResolver;
import com.hypersocket.resource.AbstractAssignableResourceService;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;

public interface UserAttributeService extends AbstractAssignableResourceService<UserAttribute>, PropertyResolver {


	public UserAttribute updateAttribute(UserAttribute attribute, String name,
			Long category, String description, String defaultValue, int weight,
			String type, String displayMode, Boolean readOnly, Boolean encrypted,
			String variableName, Set<Role> roles) throws AccessDeniedException,
			ResourceChangeException;

	public UserAttribute createAttribute(String name, Long category,
			String description, String defaultValue, int weight, String type,
			String displayMode, Boolean readOnly, Boolean encrypted, String variableName, Set<Role> roles)
			throws ResourceCreationException, AccessDeniedException;

	public void deleteAttribute(UserAttribute attribute)
			throws AccessDeniedException, ResourceChangeException;

	public Long getMaximumAttributeWeight(UserAttributeCategory cat) throws AccessDeniedException;

	public UserAttribute getAttributeByVariableName(String attributeName) throws AccessDeniedException;


}
