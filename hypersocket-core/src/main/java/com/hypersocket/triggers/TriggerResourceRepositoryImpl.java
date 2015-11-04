package com.hypersocket.triggers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.RealmService;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;
import com.hypersocket.tasks.TaskProvider;
import com.hypersocket.tasks.TaskProviderService;

@Repository
public class TriggerResourceRepositoryImpl extends
		AbstractResourceRepositoryImpl<TriggerResource> implements
		TriggerResourceRepository {
	
	static Logger log = LoggerFactory.getLogger(TriggerResourceRepositoryImpl.class);
	
	@Autowired
	TaskProviderService taskService; 
	
	@Autowired
	RealmService realmService; 
	
	@Override
	protected Class<TriggerResource> getResourceClass() {
		return TriggerResource.class;
	}

	Map<String, ResourceTemplateRepository> registeredRepository = new HashMap<String, ResourceTemplateRepository>();

	@Override
	public void registerActionRepository(TaskProvider action) {
		for (String resourceKey : action.getResourceKeys()) {
			registeredRepository.put(resourceKey, action.getRepository());
		}
	}

	@Override
	@Transactional(readOnly=true)
	public List<TriggerResource> getTriggersForEvent(final SystemEvent event) {
		
		return allEntities(TriggerResource.class,  
				new CriteriaConfiguration() {

			@Override
			public void configure(Criteria criteria) {
				switch(event.getStatus()) {
				case FAILURE:
					criteria.add(Restrictions.or(Restrictions.eq("result",
							TriggerResultType.EVENT_FAILURE), Restrictions.eq(
							"result", TriggerResultType.EVENT_ANY_RESULT)));
					break;
				case WARNING:
					criteria.add(Restrictions.or(Restrictions.eq("result",
							TriggerResultType.EVENT_WARNING), Restrictions.eq(
							"result", TriggerResultType.EVENT_ANY_RESULT)));
					break;
				default:
					criteria.add(Restrictions.or(Restrictions.eq("result",
							TriggerResultType.EVENT_SUCCESS), Restrictions.eq(
							"result", TriggerResultType.EVENT_ANY_RESULT)));
					break;
				}

				/**
				 * Get all triggers in the current realm OR the System realm.
				 */
				criteria.add(Restrictions.or(
						Restrictions.eq("realm", event.getCurrentRealm()),
						Restrictions.and(Restrictions.eq("allRealms", true) , 
								Restrictions.eq("realm", realmService.getSystemRealm()))));
				
				criteria.add(Restrictions.isNull("parentTrigger"));

				String[] events = event.getResourceKeys();
				if(!ArrayUtils.contains(events, event.getResourceKey())) {
					events = ArrayUtils.add(events, event.getResourceKey());
					if(log.isWarnEnabled()) {
						log.warn("Event " + event.getResourceKey() + " does not return its own resource key in getResourceKeys method.");
					}
				}
				
				criteria.add(Restrictions.in("event",events));
				criteria.add(Restrictions.or(Restrictions.isNull("triggerType"), 
						Restrictions.eq("triggerType", TriggerType.TRIGGER)));

			}
		});

	}

	@Override
	@Transactional(readOnly=true)
	public TriggerCondition getConditionById(Long id) {
		return get("id", id, TriggerCondition.class);
	}

	@Override
	@Transactional(readOnly=true)
	public Collection<TriggerResource> getActionsByResourceKey(final String resourceKey) {
		return list(TriggerResource.class, true, new CriteriaConfiguration() {
			
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("resourceKey", resourceKey));
			}
		});
	}

	protected void processDefaultCriteria(Criteria criteria) {
		criteria.add(Restrictions.isNull("parentTrigger"));
		criteria.add(Restrictions.eq("triggerType", TriggerType.TRIGGER));
		criteria.setFetchMode("conditions", FetchMode.SELECT);
		criteria.setFetchMode("childTriggers", FetchMode.SELECT);
	}
}
