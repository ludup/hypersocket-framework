package com.hypersocket.message;

import java.util.Collection;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;
import com.hypersocket.resource.RealmCriteria;

@Repository
public class MessageResourceRepositoryImpl extends
		AbstractResourceRepositoryImpl<MessageResource> implements
		MessageResourceRepository {

	@Override
	protected Class<MessageResource> getResourceClass() {
		return MessageResource.class;
	}
	
	@Override
	@Transactional(readOnly=true)
	public Collection<MessageResource> getMessagesByEvent(String className) {
		return list("event", className, MessageResource.class);
	}

	@Override
	@Transactional(readOnly=true)
	public MessageResource getMessageById(Integer id, Realm realm) {
		return get("messageId", id, MessageResource.class,  new RealmCriteria(realm));
	}

}
