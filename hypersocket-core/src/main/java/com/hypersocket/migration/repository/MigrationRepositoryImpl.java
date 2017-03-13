package com.hypersocket.migration.repository;

import com.hypersocket.migration.lookup.LookUpKey;
import com.hypersocket.repository.AbstractEntity;
import com.hypersocket.repository.AbstractRepositoryImpl;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public class MigrationRepositoryImpl extends AbstractRepositoryImpl<AbstractEntity> implements MigrationRepository {

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public <T> T findEntityByLookUpKey(Class<T> aClass, LookUpKey lookUpKey) {
        return (T) createCriteria(aClass).add(Restrictions.eq(lookUpKey.getProperty(), lookUpKey.getValue())).uniqueResult();
    }

    @Override
    @Transactional
    public void saveOrUpdate(AbstractEntity resource) {
        save(resource, resource.getId() == null);
    }

    @Override
    @Transactional(readOnly = true)
    @SuppressWarnings("unchecked")
    public List<AbstractEntity> findAllResourceInRealmOfType(Class aClass) {
        return createCriteria(aClass).list();
    }
}
