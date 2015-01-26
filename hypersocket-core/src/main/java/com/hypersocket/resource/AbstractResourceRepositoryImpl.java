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
import com.hypersocket.tables.ColumnSort;

@Repository
@Transactional
public abstract class AbstractResourceRepositoryImpl<T extends Resource>
		extends ResourceTemplateRepositoryImpl implements
		AbstractResourceRepository<T> {

	@Autowired
	EntityResourcePropertyStore entityPropertyStore;
	
	protected ResourcePropertyStore getPropertyStore() {
		return entityPropertyStore;
	}
	
	@Override
	public T getResourceByName(String name, Realm realm) {
		return getResourceByName(name, realm, false);
	}

	@Override
	public T getResourceByName(String name, Realm realm, boolean deleted) {
		return get("name", name, getResourceClass(), new RealmRestriction(realm), new DeletedCriteria(
				deleted));
	}

	@Override
	public T getResourceById(Long id) {
		return get("id", id, getResourceClass());
	}

	@Override
	public void deleteResource(T resource) throws ResourceChangeException {
		delete(resource);
	}

	@Override
	public void saveResource(T resource, Map<String,String> properties) {
		
		setValues(resource, properties);
		save(resource);
	}
	
	@Override
	public void updateResource(T resource, Map<String,String> properties) {
		
		setValues(resource, properties);
		save(resource);
	}


	@SuppressWarnings("unchecked")
	@Override
	public List<T> getResources(Realm realm) {

		Criteria crit = sessionFactory.getCurrentSession().createCriteria(
				getResourceClass());
		crit.setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
		crit.add(Restrictions.eq("deleted", false));
		crit.add(Restrictions.or(Restrictions.eq("realm", realm),
				Restrictions.isNull("realm")));

		return (List<T>) crit.list();
	}

	@Override
	public List<T> search(Realm realm, String searchPattern, int start,
			int length, ColumnSort[] sorting, CriteriaConfiguration... configs) {
		return super.search(getResourceClass(), "name", searchPattern, start,
				length, sorting, ArrayUtils.addAll(configs,
						new RealmAndDefaultRealmCriteria(realm)));
	}

	@Override
	public long getResourceCount(Realm realm, String searchPattern,
			CriteriaConfiguration... configs) {
		return getCount(getResourceClass(), "name", searchPattern,
				ArrayUtils.addAll(configs, new RealmAndDefaultRealmCriteria(
						realm)));
	}

	protected abstract Class<T> getResourceClass();
}
