package com.hypersocket.automation;

import java.util.ArrayList;
import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.hypersocket.resource.AbstractResourceRepositoryImpl;
import com.hypersocket.resource.TransactionOperation;
import com.hypersocket.triggers.TriggerResource;
import com.hypersocket.triggers.TriggerResourceRepository;

@Repository
public class AutomationResourceRepositoryImpl extends
		AbstractResourceRepositoryImpl<AutomationResource> implements
		AutomationResourceRepository {

	@Autowired
	TriggerResourceRepository triggerRepository;
	
	@Override
	protected Class<AutomationResource> getResourceClass() {
		return AutomationResource.class;
	}
	
	@Override
	protected void processDefaultCriteria(Criteria criteria) {
		criteria.setFetchMode("triggers", FetchMode.SELECT);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void deleteResource(AutomationResource resource, TransactionOperation<AutomationResource>... ops) {
		
		
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
