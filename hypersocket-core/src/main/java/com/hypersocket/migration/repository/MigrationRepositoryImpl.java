package com.hypersocket.migration.repository;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.CriteriaSpecification;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.migration.lookup.LookUpKey;
import com.hypersocket.migration.util.MigrationUtil;
import com.hypersocket.properties.DatabaseProperty;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmRepository;
import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.repository.AbstractRepositoryImpl;
import com.hypersocket.resource.AbstractResource;


@Repository("migrationRepository")
public class MigrationRepositoryImpl extends AbstractRepositoryImpl<AbstractEntity> implements MigrationRepository {

    @Autowired
    MigrationUtil migrationUtil;

    @Autowired
    RealmRepository realmRepository;

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public <T> T findEntityByLookUpKey(Class<T> aClass, LookUpKey lookUpKey, Realm realm) {
        Criteria criteria = createCriteria(aClass);
        String realmProperty = migrationUtil.getResourceRealmProperty(aClass);
        if(StringUtils.isNotBlank(realmProperty)) {
            criteria.add(Restrictions.eq(String.format("%s.id", realmProperty), realm.getId()));
        }
        if(lookUpKey.isComposite()) {
            String[] properties = lookUpKey.getProperties();
            Object[] values = lookUpKey.getValues();

            for (int i = 0; i < properties.length; i++) {
                criteria.add(Restrictions.eq(properties[i], values[i]));
            }
            List list = criteria.list();
            return list != null && !list.isEmpty() ? (T) list.get(0) : null;
        } else {
            List list = criteria.add(Restrictions.eq(lookUpKey.getProperty(), lookUpKey.getValue()))
                    .list();
            return list != null && !list.isEmpty() ? (T) list.get(0) : null;
        }
    }

    @Override
    @Transactional
    public void saveOrUpdate(AbstractEntity resource) {
        save(resource, resource.getId() == null);
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<AbstractEntity> findAllResourceInRealmOfType(Class aClass, Realm realm) {
        Criteria criteria = createCriteria(aClass);
        String realmProperty = migrationUtil.getResourceRealmProperty(aClass);
        if(StringUtils.isNotBlank(realmProperty)) {
            criteria.add(Restrictions.eq(String.format("%s.id", realmProperty), realm.getId()));
        }

        if(Realm.class.equals(aClass)) {
            criteria.add(Restrictions.eq("id", realm.getId()));
        }
        Order order =  Order.asc("created");
        if(aClass.isAnnotationPresent(com.hypersocket.migration.annotation.Order.class)){
            com.hypersocket.migration.annotation.Order orderAnno = (com.hypersocket.migration.annotation.Order)
                    aClass.getAnnotation(com.hypersocket.migration.annotation.Order.class);
            order = orderAnno.direction().equals(com.hypersocket.migration.annotation.Order.Direction.ASC) ?
                    Order.asc(orderAnno.property()) : Order.desc(orderAnno.property());
        }
        return criteria
                .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
                .addOrder(order)
                .list();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DatabaseProperty> findAllDatabaseProperties(AbstractResource abstractResource) {
        return realmRepository.getPropertiesForResource(abstractResource);
    }

    @SuppressWarnings("unchecked")
	@Override
    @Transactional(readOnly = true)
    public <T> T findEntityByLegacyIdInRealm(Class<? extends AbstractEntity> aClass, Long legacyId, Realm realm) {
        Criteria criteria = createCriteria(aClass);
        criteria.add(Restrictions.eq("legacyId", legacyId));
        String realmProperty = migrationUtil.getResourceRealmProperty(aClass);
        if(StringUtils.isNotBlank(realmProperty)) {
            criteria.add(Restrictions.eq(String.format("%s.id", realmProperty), realm.getId()));
        }

        return (T) criteria
                .setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY)
                .uniqueResult();
    }

    @Override
    @Transactional(readOnly = true)
    public <T> T findEntityByNameLookUpKey(Class<T> aClass, LookUpKey lookUpKey, Realm realm) {
        Criteria criteria = createCriteria(aClass);
        String realmProperty = migrationUtil.getResourceRealmProperty(aClass);
        if(StringUtils.isNotBlank(realmProperty)) {
            criteria.add(Restrictions.eq(String.format("%s.id", realmProperty), realm.getId()));
        }
        if(lookUpKey.isComposite()) {
            String[] properties = lookUpKey.getProperties();
            Object[] values = lookUpKey.getValues();
            boolean nameFound = false;
            for (int i = 0; i < properties.length; i++) {
                if("name".equals(properties[i])) {
                    criteria.add(Restrictions.eq(properties[i], values[i]));
                    nameFound = true;
                }
            }
            if(nameFound) {
                List<T> list = criteria.list();
                return list != null && !list.isEmpty() ? list.get(0) : null;
            }
        } else {
            if("name".equals(lookUpKey.getProperty())) {
                List<T> list = criteria.add(Restrictions.eq(lookUpKey.getProperty(), lookUpKey.getValue()))
                        .list();
                return list != null && !list.isEmpty() ? list.get(0) : null;
            }
        }

        return null;
    }

    @Override
    public <T> DetachedCriteria buildCriteriaFor(Class<T> aClass, String alias) {
        return DetachedCriteria.forClass(aClass, alias).
                setResultTransformer(CriteriaSpecification.DISTINCT_ROOT_ENTITY);
    }

    @Override
    @Transactional(readOnly = true)
    public List executeCriteria(DetachedCriteria criteria) {
        return criteria.getExecutableCriteria(sessionFactory.getCurrentSession()).list();
    }

    @Override
    @Transactional(readOnly = true)
    public Realm findRealm(LookUpKey lookUpKey) {
        Criteria criteria = createCriteria(Realm.class);
        if(lookUpKey.isComposite()) {
            String[] properties = lookUpKey.getProperties();
            Object[] values = lookUpKey.getValues();
            boolean nameFound = false;
            for (int i = 0; i < properties.length; i++) {
                if("name".equals(properties[i])) {
                    criteria.add(Restrictions.eq(properties[i], values[i]));
                    nameFound = true;
                }
            }
            if(nameFound) {
                List<Realm> list = criteria.list();
                return list != null && !list.isEmpty() ? list.get(0) : null;
            }
        }

        return null;
    }
}
