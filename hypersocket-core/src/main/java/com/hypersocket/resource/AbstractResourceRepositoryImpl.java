package com.hypersocket.resource;

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
import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Repository
public abstract class AbstractResourceRepositoryImpl<T extends AbstractResource>
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
				new DeletedCriteria(deleted));
	}

	@Override
	@Transactional(readOnly=true)
	public T getResourceById(Long id) {
		return get("id", id, getResourceClass());
	}


	
	@Override
	@Transactional
	public void deleteResource(T resource, @SuppressWarnings("unchecked") TransactionOperation<T>... ops) throws ResourceException {
		
		for(TransactionOperation<T> op : ops) {
			op.beforeOperation(resource, null);
		}
		
		delete(resource);
		
		for(TransactionOperation<T> op : ops) {
			op.afterOperation(resource, null);
		}
	}

	
	@Override
	public List<PropertyChange> populateEntityFields(T resource, Map<String,String> properties) {
		List<PropertyChange> changedProperties = new ArrayList<>();
		if(properties!=null) {
			for(PropertyTemplate template : getPropertyTemplates(resource)) {
				if(properties.containsKey(template.getResourceKey())) {
					/**
					 * Why was this commented out? We have to ensure we only attempt to update
					 * entity properties. Some resources use a mixture of both.
					 */
					if(template.getPropertyStore() instanceof EntityResourcePropertyStore) {
						String val = getValue(resource, template.getResourceKey());
						setValue(resource, template.getResourceKey(), properties.get(template.getResourceKey()));
						
						String newVal = getValue(resource, template.getResourceKey());
						
						/**
						 * LDP - Changed to getValue rather than use property value because the property
						 * value may not be the same as the actual value, for example in the case of enum we
						 * might have ordinal but getValue returns String.
						 */
						if(val == null) {
							val = "";
						}
						if(newVal == null) {
							newVal = "";
						}
						if(!Objects.equals(val, newVal)) {
							changedProperties.add(new PropertyChange(template.getResourceKey(), val, newVal));
						}
						properties.remove(template.getResourceKey());
					}
				}
			}
		}
		return changedProperties;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	@SafeVarargs
	public final T saveResource(T resource, Map<String,String> properties, TransactionOperation<T>... ops)  throws ResourceException {
	
		for(TransactionOperation<T> op : ops) {
			op.beforeSetProperties(resource, properties);
		}
		
		populateEntityFields(resource, properties);

		for(TransactionOperation<T> op : ops) {
			op.beforeOperation(resource, properties);
		}
		
		resource = (T) save(resource);

		// Now set any remaining values
		setValues(resource, properties);
		
		for(TransactionOperation<T> op : ops) {
			op.afterOperation(resource, properties);
		}
		
		return resource;
	}
	
	@Transactional()
	public T saveResource(T resource) throws ResourceException {
		return saveResource(resource, null);
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
	public long getResourceCount(Realm realm) {
		return getCount(getResourceClass(), "name", "",
				new RealmCriteria(realm), new DeletedCriteria(false), new DefaultCriteriaConfiguration());
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

	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly = true)
	public List<T> getResourcesByIds(Long...ids) {
		Criteria crit = createCriteria(getResourceClass());
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit.add(Restrictions.eq("deleted", false));
		crit.add(Restrictions.in("id", ids));

		processDefaultCriteria(crit);

		return (List<T>) crit.list();
	}

	@Override
	@Transactional
	public void deleteResources(List<T> resources, @SuppressWarnings("unchecked") TransactionOperation<T>... ops) throws ResourceException {
		for (T resource: resources) {
			deleteResource(resource, ops);
		}
	}
}
