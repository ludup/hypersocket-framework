package com.hypersocket.realm;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.repository.DeletedCriteria;
import com.hypersocket.repository.HiddenCriteria;
import com.hypersocket.repository.PrincipalTypesCriteria;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;
import com.hypersocket.resource.RealmCriteria;
import com.hypersocket.tables.ColumnSort;

@Repository
public class PrincipalRepositoryImpl extends AbstractResourceRepositoryImpl<Principal> implements PrincipalRepository {

	@Override
	@Transactional(readOnly=true)
	public List<Principal> search(Realm realm, PrincipalType type, String searchColumn, String searchPattern, int start, int length,
			ColumnSort[] sorting) {
		return super.search(realm, searchColumn, searchPattern, start, length, sorting, new DeletedCriteria(false), new PrincipalTypeCriteria(type));
	}

	@Override
	@Transactional(readOnly=true)
	public long getResourceCount(Realm realm,  PrincipalType type,String searchColumn, String searchPattern) {
		return super.getResourceCount(realm, searchColumn, searchPattern, new DeletedCriteria(false), new PrincipalTypeCriteria(type));
	}
	
	@Override
	@Transactional(readOnly=true)
	public long getResourceCount(Collection<Realm> realms,  PrincipalType type) {
		return super.getResourceCount(realms, "", "", new DeletedCriteria(false), new PrincipalTypeCriteria(type));
	}
	
	@Override
	@Transactional(readOnly=true)
	public long getResourceCount(Realm realm,  PrincipalType type) {
		return super.getResourceCount(realm, "", "", new DeletedCriteria(false), new PrincipalTypeCriteria(type));
	}

	@Override
	@Transactional(readOnly=true)
	public Collection<Principal> getPrincpalsByName(final String username, final PrincipalType... types) {
		return list(Principal.class, new DeletedCriteria(false), new PrincipalTypesCriteria(types), new CriteriaConfiguration() {
			
			@Override
			public void configure(Criteria criteria) {
				criteria.add(Restrictions.or(Restrictions.eq("name", username).ignoreCase(),
						Restrictions.eq("primaryEmail", username).ignoreCase()));
			}
		});
	}
	
	@Override
	@Transactional(readOnly=true)
	public Collection<Principal> getPrincpalsByName(final String username, Realm realm, final PrincipalType... types) {
		return list(Principal.class, new RealmCriteria(realm), new DeletedCriteria(false), new PrincipalTypesCriteria(types), new CriteriaConfiguration() {
			
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
	@Transactional(readOnly=true)
	public Collection<Principal> allPrincipals() {
		return allEntities(Principal.class, new DeletedCriteria(false), new HiddenCriteria(false));
	}
	
	public boolean isDeletable() {
		return false;
	}

	@Override
	public Principal getPrincipalByReference(String reference, Realm realm) {
		if(NumberUtils.isCreatable(reference)) {
			return getResourceById(Long.parseLong(reference));
		} else {
			return getResourceByName(reference, realm);
		}
	}
}
