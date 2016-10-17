package com.hypersocket.message;

import java.util.Collection;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceRepository;

public interface MessageResourceRepository extends
		AbstractResourceRepository<MessageResource> {

	Collection<MessageResource> getMessagesByEvent(String className);

	MessageResource getMessageById(Integer id, Realm realm);

}
