package com.hypersocket.realm;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.repository.CriteriaConfiguration;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;
import com.hypersocket.tables.ColumnSort;

@Repository
public class PrincipalRepositoryImpl extends AbstractResourceRepositoryImpl<Principal> implements PrincipalRepository {

	@Override
	@Transactional(readOnly=true)
	public List<Principal> search(Realm realm, PrincipalType type, String searchColumn, String searchPattern, int start, int length,
			ColumnSort[] sorting) {
		return super.search(realm, searchColumn, searchPattern, start, length, sorting, new PrincipalTypeCriteria(type));
	}

	@Override
	@Transactional(readOnly=true)
	public long getResourceCount(Realm realm,  PrincipalType type,String searchColumn, String searchPattern) {
		return super.getResourceCount(realm, searchColumn, searchPattern, new PrincipalTypeCriteria(type));
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
}
