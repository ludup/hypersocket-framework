package com.hypersocket.message;

import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.realm.Realm;
import com.hypersocket.repository.CriteriaConfiguration;
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
	public MessageResource getMessageById(String id, Realm realm) {
		return get("resourceKey", id, MessageResource.class,  new RealmCriteria(realm));
	}

	@Override
	@Transactional(readOnly=true)
	public boolean hasMissingMessages(final Realm realm, final List<String> resourceKeys) {
		return getCount(MessageResource.class, new RealmCriteria(realm), new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.in("resourceKey", resourceKeys));
			}
			
		}) != resourceKeys.size();
	}

}
