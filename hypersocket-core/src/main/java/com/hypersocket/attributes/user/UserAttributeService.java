package com.hypersocket.attributes.user;

import com.hypersocket.attributes.AttributeService;
import com.hypersocket.realm.events.UserEvent;

public interface UserAttributeService extends AttributeService<UserAttribute, UserAttributeCategory> {

	void onApplicationEvent(UserEvent event);
}
