package com.hypersocket.automation;

import java.util.ArrayList;
import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.TransactionOperation;
import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.TriggerResource;

@Repository
public class AutomationResourceRepositoryImpl extends
		AbstractResourceRepositoryImpl<AutomationResource> implements
		AutomationResourceRepository {
	
	@Override
	protected Class<AutomationResource> getResourceClass() {
		return AutomationResource.class;
	}
	
	@Override
	protected void processDefaultCriteria(Criteria criteria) {
		super.processDefaultCriteria(criteria);
		criteria.setFetchMode("triggers", FetchMode.SELECT);
	}

	@Override
	public void deleteRealm(Realm realm) {
		super.deleteRealm(realm);
		
		/* The parent task may have been orphaned, so explictly delete those too */
		deleteResourcesOfClassFromRealm(realm, Task.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	public void deleteResource(AutomationResource resource, TransactionOperation<AutomationResource>... ops) throws ResourceException {
		
		
		Collection<TriggerResource> triggers = new ArrayList<TriggerResource>(resource.getChildTriggers());
		for(TriggerResource trigger : triggers) {
			recurseDetach(trigger);
		}
		
		resource.getChildTriggers().clear();
		save(resource);
		flush();
		super.deleteResource(resource, ops);
		
		for(TriggerResource trigger : triggers) {
			recurseDelete(trigger);
		}
		flush();
		
	}

	private void recurseDelete(TriggerResource trigger) {
		for(TriggerResource child : trigger.getChildTriggers()) {
			recurseDelete(child);
		}
		delete(trigger);
	}

	private void recurseDetach(TriggerResource trigger) {
		for(TriggerResource child : trigger.getChildTriggers()) {
			recurseDetach(child);
		}
		trigger.setParentTrigger(null);
		save(trigger);
	}

}
