package com.hypersocket.automation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRestriction;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.resource.TransactionOperation;
import com.hypersocket.tables.ColumnSort;
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
	
	@Transactional(readOnly=true)
	@Override
	public AutomationResource getAutomationById(Long id, Realm currentRealm) {
		return get("id", id, AutomationResource.class, new RealmRestriction(currentRealm));
	}
	
	@Transactional(readOnly=true)
	@Override
	public List<?> getCsvAutomations(Realm realm, String searchColumn, String searchPattern, int start, int length,
			ColumnSort[] sorting) {

		Criteria criteria = createCriteria(AutomationResource.class);
		
		if (StringUtils.isNotBlank(searchPattern)) {
			criteria.add(Restrictions.ilike("name", searchPattern));
		}
		

		criteria.add(Restrictions.eq("realm", realm)).add(Restrictions.eq("resourceKey", "auditLogReportGeneration"));

		
		
		return criteria.list();
	}
	
	@Transactional(readOnly=true)
	@Override
	public Long getCsvAutomationsCount(Realm realm, String searchColumn, String searchPattern) {
		Criteria criteria = createCriteria(AutomationResource.class);
		if (StringUtils.isNotBlank(searchPattern)) {
			criteria.add(Restrictions.ilike("name", searchPattern));
		}
		criteria.add(Restrictions.eq("realm", realm)).add(Restrictions.eq("resourceKey", "auditLogReportGeneration"));
		return new Long(criteria.list().size());
	}

}
