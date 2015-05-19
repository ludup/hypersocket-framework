package com.hypersocket.resource;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.ArrayUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.properties.EntityResourcePropertyStore;
import com.hypersocket.properties.ResourcePropertyStore;
import com.hypersocket.properties.ResourceTemplateRepositoryImpl;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRestriction;
import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.repository.DeletedDetachedCriteria;
import com.hypersocket.tables.ColumnSort;

@Repository
public abstract class AbstractResourceRepositoryImpl<T extends Resource>
		extends ResourceTemplateRepositoryImpl implements
		AbstractResourceRepository<T> {

	@Autowired
	EntityResourcePropertyStore entityPropertyStore;
	
	protected ResourcePropertyStore getPropertyStore() {
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
		return get("name", name, getResourceClass(), new RealmRestriction(realm), new DeletedDetachedCriteria(
				deleted));
	}

	@Override
	@Transactional(readOnly=true)
	public T getResourceById(Long id) {
		return get("id", id, getResourceClass());
	}

	@Override
	@Transactional
	public void deleteResource(T resource) {
		delete(resource);
	}

	protected void beforeSave(T resource, Map<String,String> properties) {
		
	}
	
	protected void afterSave(T resource, Map<String,String> properties) {
		
	}
	
	@Override
	@Transactional
	@SafeVarargs
	public final void saveResource(T resource, Map<String,String> properties, TransactionOperation<T>... ops) {
		
		setValues(resource, properties);

		beforeSave(resource, properties);
		
		for(TransactionOperation<T> op : ops) {
			op.beforeOperation(resource, properties);
		}
		
		save(resource);
		
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
		return (List<T>) crit.list();
	}

	@Override
	@Transactional(readOnly=true)
	public List<T> search(Realm realm, String searchPattern, int start,
			int length, ColumnSort[] sorting, CriteriaConfiguration... configs) {
		return super.search(getResourceClass(), "name", searchPattern, start,
				length, sorting, ArrayUtils.addAll(configs,
						new RealmCriteria(realm)));
	}

	@Override
	@Transactional(readOnly=true)
	public long getResourceCount(Realm realm, String searchPattern,
			CriteriaConfiguration... configs) {
		return getCount(getResourceClass(), "name", searchPattern,
				ArrayUtils.addAll(configs, new RealmCriteria(
						realm)));
	}

	
	@Override
	public long allRealmsResourcesCount() {
		return getCount(getResourceClass(), new DeletedCriteria(false));
	}
	
	protected abstract Class<T> getResourceClass();
}
