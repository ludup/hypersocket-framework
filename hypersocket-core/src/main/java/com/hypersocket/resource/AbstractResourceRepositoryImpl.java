package com.hypersocket.resource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.encrypt.EncryptionService;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.properties.ResourcePropertyStore;
import com.hypersocket.properties.ResourceTemplateRepositoryImpl;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRestriction;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.tables.ColumnSort;

@Repository
public abstract class AbstractResourceRepositoryImpl<T extends Resource>
		extends ResourceTemplateRepositoryImpl implements
		AbstractResourceRepository<T> {

	protected EntityResourcePropertyStore entityPropertyStore;

	@Autowired
	EncryptionService encryptionService;
	
	@PostConstruct
	private void postConstruct() {
		entityPropertyStore = new EntityResourcePropertyStore(encryptionService);
	}
	
	protected ResourcePropertyStore getPropertyStore() {
		return entityPropertyStore;
	}
	
	@Override
	public EntityResourcePropertyStore getEntityStore() {
		return entityPropertyStore;
	}
	
	@Override
	@Transactional(readOnly=true)
	public T getResourceByName(String name, Realm realm) {
		return getResourceByName(name, realm, false);
	}

	@Override
	@Transactional(readOnly=true)
	public T getResourceByName(String name, Realm realm, boolean deleted) {
		return get("name", name, getResourceClass(), 
				new RealmRestriction(realm), 
				new DefaultDetatchedCriteriaConfiguration(), 
				new DeletedCriteria(
				deleted));
	}

	@Override
	@Transactional(readOnly=true)
	public T getResourceById(Long id) {
		return get("id", id, getResourceClass());
	}

	@Override
	@Transactional
	public void deleteResource(T resource, @SuppressWarnings("unchecked") TransactionOperation<T>... ops) {
		
		for(TransactionOperation<T> op : ops) {
			op.beforeOperation(resource, null);
		}
		
		delete(resource);
		
		for(TransactionOperation<T> op : ops) {
			op.afterOperation(resource, null);
		}
	}

	protected void beforeSave(T resource, Map<String,String> properties) {
		
	}
	
	protected void afterSave(T resource, Map<String,String> properties) {
		
	}
	
	@Override
	public List<PropertyChange> populateEntityFields(T resource, Map<String,String> properties) {
		List<PropertyChange> changedProperties = new ArrayList<>();
		final Set<String> propertyNames = getPropertyNames(resource);
		for(String resourceKey : propertyNames) {
			if(properties.containsKey(resourceKey)) {
				int x =1;
				PropertyTemplate template = getPropertyTemplate(resource, resourceKey);
//				if(template.getPropertyStore() instanceof EntityResourcePropertyStore) {
					String val = getValue(resource, resourceKey);
					String newVal = properties.get(resourceKey);
					
					/* NOTE - I am not 100% sure about this. What if a value IS changing to (or from) null? */
					
					if(val == null) 
						val = "";
					if(newVal == null) 
						newVal = "";
					
					if(!Objects.equals(val, newVal)) {
						changedProperties.add(new PropertyChange(resourceKey, val, newVal));
					}
					
					setValue(resource, resourceKey, newVal);
					properties.remove(resourceKey);
//				}
			}
		}
		return changedProperties;
	}
	
	@Override
	@Transactional
	@SafeVarargs
	public final void saveResource(T resource, Map<String,String> properties, TransactionOperation<T>... ops) {
		
		beforeSave(resource, properties);
		
		for(TransactionOperation<T> op : ops) {
			op.beforeOperation(resource, properties);
		}
		
		save(resource);

		// Now set any remaining values
		setValues(resource, properties);
		
		afterSave(resource, properties);
		
		for(TransactionOperation<T> op : ops) {
			op.afterOperation(resource, properties);
		}
		
	}


	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public List<T> getResources(Realm realm) {

		Criteria crit = createCriteria(getResourceClass());
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit.add(Restrictions.eq("deleted", false));
		if(realm==null) {
			crit.add(Restrictions.isNull("realm"));
		} else {
			crit.add(Restrictions.eq("realm", realm));
		}
		
		processDefaultCriteria(crit);
		
		return (List<T>) crit.list();
	}

	@Override
	@Transactional(readOnly=true)
	public List<T> search(Realm realm, String searchColumn, String searchPattern, int start,
			int length, ColumnSort[] sorting, CriteriaConfiguration... configs) {
		return super.search(getResourceClass(), searchColumn, searchPattern, start,
				length, sorting, ArrayUtils.addAll(configs,
						new RealmCriteria(realm), new DeletedCriteria(false), new DefaultCriteriaConfiguration()));
	}

	@Override
	@Transactional(readOnly=true)
	public long getResourceCount(Realm realm, String searchColumn, String searchPattern,
			CriteriaConfiguration... configs) {
		return getCount(getResourceClass(), searchColumn, searchPattern,
				ArrayUtils.addAll(configs, new RealmCriteria(
						realm), new DeletedCriteria(false), new DefaultCriteriaConfiguration()));
	}

	
	@Override
	@Transactional(readOnly=true)
	public long allRealmsResourcesCount() {
		return getCount(getResourceClass(), new DeletedCriteria(false), new DefaultCriteriaConfiguration());
	}
	
	protected void processDefaultCriteria(Criteria criteria) {
		
	}


	class DefaultDetatchedCriteriaConfiguration implements CriteriaConfiguration {

		@Override
		public void configure(Criteria criteria) {
			processDefaultCriteria(criteria);
		}
		
	}
	
	class DefaultCriteriaConfiguration implements CriteriaConfiguration {

		@Override
		public void configure(Criteria criteria) {
			processDefaultCriteria(criteria);
		}
		
	}
	protected abstract Class<T> getResourceClass();
}
