package com.hypersocket.triggers;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.events.SystemEvent;
import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;

@Repository
@Transactional
public class TriggerResourceRepositoryImpl extends
		AbstractResourceRepositoryImpl<TriggerResource> implements
		TriggerResourceRepository {

	@Autowired
	TriggerResourceService resourceService;

	@Override
	public void updateResource(TriggerResource resource,
			Map<String, String> properties) {

		// Reverse the operation for update due to transient properties needing
		// updating first.
		for (TriggerAction action : resource.getActions()) {

			if(action.getProperties()!=null) {
				TriggerActionProvider provider = resourceService
						.getActionProvider(action.getResourceKey());
				for (Map.Entry<String, String> e : action.getProperties()
						.entrySet()) {
					provider.getRepository().setValue(action, e.getKey(),
							e.getValue());
				}
			}
		}

		super.updateResource(resource, properties);

	}

	@Override
	public void saveResource(TriggerResource resource,
			Map<String, String> properties) {

		super.saveResource(resource, properties);

		for (TriggerAction action : resource.getActions()) {

			if(action.getProperties()!=null) {
				TriggerActionProvider provider = resourceService
						.getActionProvider(action.getResourceKey());
				
				for (Map.Entry<String, String> e : action.getProperties()
						.entrySet()) {
					provider.getRepository().setValue(action, e.getKey(),
							e.getValue());
				}
			}
		}
	}

	@Override
	protected Class<TriggerResource> getResourceClass() {
		return TriggerResource.class;
	}

	Map<String, ResourceTemplateRepository> registeredRepository = new HashMap<String, ResourceTemplateRepository>();

	@Override
	public void registerActionRepository(TriggerActionProvider action) {
		for (String resourceKey : action.getResourceKeys()) {
			registeredRepository.put(resourceKey, action.getRepository());
		}
	}

	@Override
	public List<TriggerResource> getTriggersForEvent(final SystemEvent event) {

		return allEntities(TriggerResource.class, new CriteriaConfiguration() {

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

				criteria.add(Restrictions.eq("event", event.getResourceKey()));
			}
		});

	}

	// @Override
	// public void assignAction(TriggerResource trigger, TriggerAction action,
	// Map<String, String> properties) {
	//
	// // Save the action
	// action.setTrigger(trigger);
	// save(action);
	//
	// ResourceTemplateRepository repository = registeredRepository.get(action
	// .getResourceKey());
	//
	// // Save properties
	// if (properties != null) {
	// for (Map.Entry<String, String> e : properties.entrySet()) {
	// repository.setValue(action, e.getKey(), e.getValue());
	// }
	// }
	//
	// refresh(action);
	// refresh(trigger);
	// }

	@Override
	public TriggerAction getActionById(Long id) {
		return get("id", id, TriggerAction.class);
	}

	@Override
	public TriggerCondition getConditionById(Long id) {
		return get("id", id, TriggerCondition.class);
	}

	@Override
	public Collection<TriggerAction> getActionsByResourceKey(final String resourceKey) {
		return list(TriggerAction.class, true, new CriteriaConfiguration() {
			
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.eq("resourceKey", resourceKey));
			}
		});
	}

}
