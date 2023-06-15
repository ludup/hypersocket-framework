package com.hypersocket.resource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.hypersocket.auth.AuthenticationService;
import com.hypersocket.properties.AbstractPropertyTemplate;
import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.properties.ResourcePropertyStore;
import com.hypersocket.properties.ResourceTemplateRepositoryImpl;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRestriction;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.tables.ColumnSort;
import com.hypersocket.transactions.TransactionCallbackWithError;

@Repository
public abstract class AbstractSimpleResourceRepositoryImpl<T extends SimpleResource>
		extends ResourceTemplateRepositoryImpl implements
		AbstractSimpleResourceRepository<T> {

	protected EntityResourcePropertyStore entityPropertyStore;

	@Autowired
	@Qualifier("transactionManager")
	private PlatformTransactionManager txManager;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	private List<TransactionOperation<T>> defaultOperations = new ArrayList<TransactionOperation<T>>();
	
	@PostConstruct
	private void postConstruct() {
		entityPropertyStore = new EntityResourcePropertyStore(applicationContext, getResourceClass().getCanonicalName());
	}
	
	protected void addDefaultOperation(TransactionOperation<T> op) {
		defaultOperations.add(op);
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
	public T getResourceByReference(String reference, Realm realm) {
		if(NumberUtils.isCreatable(reference)) {
			return getResourceById(Long.parseLong(reference));
		} else {
			return getResourceByName(reference, realm);
		}
	}
	
	@Override
	@Transactional(readOnly=true)
	public T getResourceById(Long id) {
		return get("id", id, getResourceClass());
	}
	
	@Transactional(readOnly=true)
	public T getResourceByUUID(String id) {
		return get("reference", id, getResourceClass());
	}
	
	@Override
	@Transactional(readOnly=true)
	public T getResourceByLegacyId(Long id) {
		return get("legacyId", id, getResourceClass());
	}

	protected boolean isSoftDelete() {
		return false;
	}

	@Override
	@Transactional
	public void deleteResource(T resource, @SuppressWarnings("unchecked") TransactionOperation<T>... ops) throws ResourceException {
		
		for(TransactionOperation<T> op : ops) {
			op.beforeOperation(resource, null);
		}
		
		if(isSoftDelete()) {
			resource.setDeleted(true);
			save(resource);
		} else {
			delete(resource);
		}
		for(TransactionOperation<T> op : ops) {
			op.afterOperation(resource, null);
		}
	}
	
	@Override
	public List<PropertyChange> calculateChanges(T resource, Map<String,String> properties) {
		List<AbstractPropertyTemplate> propertyTemplates = new ArrayList<AbstractPropertyTemplate>();
		propertyTemplates.addAll(getPropertyTemplates(resource));
		return calculateChanges(resource, propertyTemplates, properties);
	}
	
	@Override
	public List<PropertyChange> calculateChanges(T resource, 
			Collection<AbstractPropertyTemplate> propertyTemplates,
			Map<String, String> properties) {
		List<PropertyChange> changedProperties = new ArrayList<>();
		if(properties!=null) {
			for(AbstractPropertyTemplate template : propertyTemplates) {
				if(properties.containsKey(template.getResourceKey())) {
					String val = getValue(resource, template.getResourceKey());
					String newVal =  properties.get(template.getResourceKey());
					if(val == null) {
						val = "";
					}
					if(newVal == null) {
						newVal = "";
					}
					if(!Objects.equals(val, newVal)) {
						changedProperties.add(new PropertyChange(template.getResourceKey(), val, newVal));
					}
				}
			}
		}
		return changedProperties;
	}
	
	public void populateEntityFields(T resource, Map<String, String> properties) {
		if (properties != null) {
			for (PropertyTemplate template : getPropertyTemplates(resource)) {
				if (template.getPropertyStore() instanceof EntityResourcePropertyStore && properties.containsKey(template.getResourceKey())) {
					setValue(resource, template.getResourceKey(), properties.get(template.getResourceKey()));
					properties.remove(template.getResourceKey());
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional
	@SafeVarargs
	public final List<PropertyChange> saveResource(T resource, Map<String,String> properties, TransactionOperation<T>... ops)  throws ResourceException {
	
		List<TransactionOperation<T>> operations = new ArrayList<TransactionOperation<T>>();
		if(!defaultOperations.isEmpty()) {
			operations.addAll(defaultOperations);
		}
		
		Collections.addAll(operations, ops);
		
		for(TransactionOperation<T> op : operations) {
			op.beforeSetProperties(resource, properties);
		}

		final List<PropertyChange> changes = calculateChanges(resource, properties);
		populateEntityFields(resource, properties);

		for(TransactionOperation<T> op : operations) {
			op.beforeOperation(resource, properties);
		}
		
		beforeSave(resource, properties);
		
		resource = (T) save(resource);

		// Now set any remaining values
		setValues(resource, properties);
		
		clearPropertyCache(resource);
		
		afterSave(resource, properties);
		
		for(TransactionOperation<T> op : operations) {
			op.afterOperation(resource, properties);
		}
		
		return changes;
	}
	
	@Override
	@Transactional
	public void touch(T resource) {
		save(resource);
	}
	
	protected void afterSave(T resource, Map<String, String> properties) {

	}

	protected void beforeSave(T resource, Map<String, String> properties) {
		
	}

	@Transactional
	public List<PropertyChange> saveResource(T resource) throws ResourceException {
		return saveResource(resource, null);
	}

	
	@Override
	public Collection<T> list(CriteriaConfiguration... configs) {
		return list(getResourceClass(), configs);
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
	
	@SuppressWarnings("unchecked")
	@Override
	@Transactional(readOnly=true)
	public List<T> getResources(Realm realm, boolean includeDeleted) {

		Criteria crit = createCriteria(getResourceClass());
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		if(includeDeleted) {
			crit.add(Restrictions.eq("deleted", false));
		}
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
		return getCount(getResourceClass(),
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
	public long getResourceCount(Collection<Realm> realms, String searchColumn, String searchPattern,
			CriteriaConfiguration... configs) {
		return getCount(getResourceClass(), searchColumn, searchPattern,
				ArrayUtils.addAll(configs, new RealmsCriteria(
						realms), new DeletedCriteria(false), new DefaultCriteriaConfiguration()));
	}

	
	@Override
	@Transactional(readOnly=true)
	public long allRealmsResourcesCount() {
		return getCount(getResourceClass(), new DeletedCriteria(false), new DefaultCriteriaConfiguration());
	}
	
	@Override
	@Transactional(readOnly=true)
	public Collection<T> allRealmsResources() {
		return list(getResourceClass(), new DeletedCriteria(false), new DefaultCriteriaConfiguration());
	}
	
	protected void processDefaultCriteria(Criteria criteria) {
		
	}


	class DefaultDetatchedCriteriaConfiguration implements CriteriaConfiguration {

		@Override
		public void configure(Criteria criteria) {
			processDefaultCriteria(criteria);
		}
		
	}
	
	public class DefaultCriteriaConfiguration implements CriteriaConfiguration {

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
	public void deleteResources(Collection<T> resources, @SuppressWarnings("unchecked") TransactionOperation<T>... ops) throws ResourceException {
		for (T resource: resources) {
			deleteResource(resource, ops);
		}
	}
	
	@Override
	public <E> E doInTransaction(TransactionCallback<E> transaction) throws ResourceException {

		TransactionTemplate tmpl = new TransactionTemplate(txManager);
		try {
			return tmpl.execute(transaction);
		} catch (Throwable e) {
			if (transaction instanceof TransactionCallbackWithError) {
				((TransactionCallbackWithError<E>) transaction).doTransacationError(e);
			}
			if (e.getCause() instanceof ResourceException) {
				throw (ResourceException) e.getCause();
			} 
			throw new ResourceException(AuthenticationService.RESOURCE_BUNDLE, 
					"error.transactionFailed",
					e.getMessage());
		}
	}
}
