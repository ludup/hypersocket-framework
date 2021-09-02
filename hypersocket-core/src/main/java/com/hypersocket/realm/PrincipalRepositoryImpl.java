package com.hypersocket.realm;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.repository.HiddenCriteria;
import com.hypersocket.repository.LocallyDeletedCriteria;
import com.hypersocket.repository.PrincipalStatusCriteria;
import com.hypersocket.repository.PrincipalSuspendedCriteria;
import com.hypersocket.repository.PrincipalTypesCriteria;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;
import com.hypersocket.resource.RealmCriteria;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.tables.ColumnSort;

@Repository
public class PrincipalRepositoryImpl extends AbstractResourceRepositoryImpl<Principal> implements PrincipalRepository {
	final static Logger LOG = LoggerFactory.getLogger(PrincipalRepositoryImpl.class);

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public void deleteRealm(Realm realm) {
		Query q = sessionFactory.getCurrentSession().createSQLQuery(
				"delete l from principal_links l left join principals p ON p.resource_id = l.principals_resource_id WHERE p.realm_id = "
						+ realm.getId());
		q.executeUpdate();
		q = sessionFactory.getCurrentSession().createSQLQuery(
				"delete l from principal_links l left join principals p ON p.resource_id = l.linkedPrincipals_resource_id WHERE p.realm_id = "
						+ realm.getId());
		q.executeUpdate();
	}

	@Override
	@Transactional(readOnly = true)
	public List<Principal> search(Realm realm, PrincipalType type, String searchColumn, String searchPattern, int start,
			int length, ColumnSort[] sorting) {
		return super.search(realm, searchColumn, searchPattern, start, length, sorting, new DeletedCriteria(false),
				new PrincipalTypeCriteria(type));
	}

	@Override
	@Transactional(readOnly = true)
	public long getResourceCount(Realm realm, PrincipalType type, String searchColumn, String searchPattern) {
		return super.getResourceCount(realm, searchColumn, searchPattern, new DeletedCriteria(false),
				new PrincipalTypeCriteria(type));
	}

	@Override
	@Transactional(readOnly = true)
	public long getResourceCount(Collection<Realm> realms, PrincipalType type) {
		return super.getResourceCount(realms, "", "", new DeletedCriteria(false), new PrincipalTypeCriteria(type));
	}

	@Override
	@Transactional(readOnly = true)
	public long getResourceCount(Realm realm, PrincipalType type) {
		return super.getResourceCount(realm, "", "", new DeletedCriteria(false), new PrincipalTypeCriteria(type));
	}

	@Override
	@Transactional(readOnly = true)
	public Collection<Principal> getPrincpalsByName(final String username, final PrincipalType... types) {
		return list(Principal.class, new DeletedCriteria(false), new PrincipalTypesCriteria(types),
				new CriteriaConfiguration() {

					@Override
					public void configure(Criteria criteria) {
						criteria.add(Restrictions.or(Restrictions.eq("name", username).ignoreCase(),
								Restrictions.eq("primaryEmail", username).ignoreCase()));
					}
				});
	}

	@Override
	@Transactional(readOnly = true)
	public Collection<Principal> getPrincpalsByName(final String username, Realm realm, final PrincipalType... types) {
		return list(Principal.class, new RealmCriteria(realm), new DeletedCriteria(false),
				new PrincipalTypesCriteria(types), new CriteriaConfiguration() {

					@Override
					public void configure(Criteria criteria) {
						criteria.add(Restrictions.or(Restrictions.eq("name", username).ignoreCase(),
								Restrictions.eq("primaryEmail", username).ignoreCase()));
					}
				});
	}

	@Override
	protected Class<Principal> getResourceClass() {
		return Principal.class;
	}

	class PrincipalTypeCriteria implements CriteriaConfiguration {

		PrincipalType type;

		PrincipalTypeCriteria(PrincipalType type) {
			this.type = type;
		}

		@Override
		public void configure(Criteria criteria) {
			criteria.add(Restrictions.eq("principalType", type));
		}

	}

	@Override
	@Transactional(readOnly = true)
	public Collection<Principal> allPrincipals() {
		return allEntities(Principal.class, new DeletedCriteria(false), new HiddenCriteria(false));
	}

	public boolean isDeletable() {
		return true;
	}

	@Override
	public Principal getPrincipalByReference(String reference, Realm realm) {
		if (NumberUtils.isCreatable(reference)) {
			return getResourceById(Long.parseLong(reference));
		} else {
			return getResourceByName(reference, realm);
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<?> searchDeleted(Class<?> clazz, Realm realm, PrincipalType type, String searchColumn, String searchPattern,
			ColumnSort[] sorting, int start, int length, boolean local, CriteriaConfiguration... criteriaConfiguration) {
		return super.search(clazz, searchColumn, searchPattern, start, length, sorting,
				ArrayUtils.addAll(criteriaConfiguration, new RealmCriteria(realm), new PrincipalTypeCriteria(type),
						local ? new LocallyDeletedCriteria(true): new DeletedCriteria(true), new DefaultCriteriaConfiguration()));
	}

	@Override
	@Transactional(readOnly = true)
	public Long getDeletedCount(Class<?> clazz, Realm realm, PrincipalType type, String searchColumn, String searchPattern,
			boolean local, CriteriaConfiguration... criteriaConfiguration) {
		return getCount(clazz, searchColumn, searchPattern,
				ArrayUtils.addAll(criteriaConfiguration, new RealmCriteria(realm), new PrincipalTypeCriteria(type),
						local ? new LocallyDeletedCriteria(true): new DeletedCriteria(true), new DefaultCriteriaConfiguration()));
	}
	
	@Override
	@Transactional(readOnly = true)
	public List<?> searchSuspendedState(Class<?> clazz, Realm realm, PrincipalType type, String searchColumn, String searchPattern,
			ColumnSort[] sorting, int start, int length, boolean suspended, CriteriaConfiguration... criteriaConfiguration) {
		return super.search(clazz, searchColumn, searchPattern, start, length, sorting,
				ArrayUtils.addAll(criteriaConfiguration, new RealmCriteria(realm), new PrincipalTypeCriteria(type),
						new PrincipalSuspendedCriteria(suspended), new DeletedCriteria(false), new DefaultCriteriaConfiguration()));
	}

	@Override
	@Transactional(readOnly = true)
	public Long getSuspendedStateCount(Class<?> clazz, Realm realm, PrincipalType type, String searchColumn, String searchPattern,
			boolean suspended, CriteriaConfiguration... criteriaConfiguration) {
		return getCount(clazz, searchColumn, searchPattern,
				ArrayUtils.addAll(criteriaConfiguration, new RealmCriteria(realm), new PrincipalTypeCriteria(type),
						new PrincipalSuspendedCriteria(suspended), new DeletedCriteria(false), new DefaultCriteriaConfiguration()));
	}

	@Override
	@Transactional
	public void undelete(Realm realm, Principal user) throws ResourceChangeException {
		user.setLocallyDeleted(false);
		user.setDeleted(false);
		save(user);
		flush();
	}

	@Override
	@Transactional(readOnly = true)
	public List<?> searchRemoteUserWithPrincipalStatus(Class<?> clazz, Realm realm, PrincipalType type, String searchColumn,
			String searchPattern, ColumnSort[] sorting, int start, int length, List<PrincipalStatus> principalStatuses,
			CriteriaConfiguration... criteriaConfiguration) {
		return super.search(clazz, searchColumn, searchPattern, start, length, sorting,
				ArrayUtils.addAll(criteriaConfiguration, new RealmCriteria(realm), new PrincipalTypeCriteria(type),
						new PrincipalStatusCriteria(principalStatuses.toArray(new PrincipalStatus[0])), new DeletedCriteria(false), new DefaultCriteriaConfiguration()));
	}

	@Override
	@Transactional(readOnly = true)
	public Long getRemoteUserWithPrincipalStatusCount(Class<?> clazz, Realm realm, PrincipalType type, String searchColumn,
			String searchPattern, List<PrincipalStatus> principalStatuses, CriteriaConfiguration... criteriaConfiguration) {
		return getCount(clazz, searchColumn, searchPattern,
				ArrayUtils.addAll(criteriaConfiguration, new RealmCriteria(realm), new PrincipalTypeCriteria(type),
						new PrincipalStatusCriteria(principalStatuses.toArray(new PrincipalStatus[0])), new DeletedCriteria(false), new DefaultCriteriaConfiguration()));
	}
}
