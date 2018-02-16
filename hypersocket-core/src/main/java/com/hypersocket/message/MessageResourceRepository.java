package com.hypersocket.message;

import java.util.Collection;
import java.util.List;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceRepository;

public interface MessageResourceRepository extends
		AbstractResourceRepository<MessageResource> {

	Collection<MessageResource> getMessagesByEvent(String className);

	MessageResource getMessageById(String resourceKey, Realm realm);

	boolean hasMissingMessages(Realm realm, List<String> messageIds);

}
