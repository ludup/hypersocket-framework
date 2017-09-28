package com.hypersocket.attributes.role;

import com.hypersocket.attributes.AttributeService;
import com.hypersocket.role.events.RoleEvent;

public interface RoleAttributeService extends AttributeService<RoleAttribute, RoleAttributeCategory> {

	void onApplicationEvent(RoleEvent event);
}
