package com.hypersocket.realm;

import java.util.Collection;

import org.hibernate.Criteria;
import org.hibernate.criterion.Restrictions;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.repository.AbstractRepositoryImpl;
import com.hypersocket.repository.CriteriaConfiguration;

@Repository
public class PrincipalSuspensionRepositoryImpl extends
		AbstractRepositoryImpl<Long> implements PrincipalSuspensionRepository {

	@Override
	@Transactional
	public void createPrincipalSuspension(
			PrincipalSuspension principalSuspension) {
		save(principalSuspension);
	}

	@Override
	@Transactional(readOnly = true)
	public Collection<PrincipalSuspension> getSuspensions(Principal principal, final PrincipalSuspensionType type) {
		return list("principal", principal, PrincipalSuspension.class, new CriteriaConfiguration() {
			
			@Override
			public void configure(Criteria criteria) {
				if(type==PrincipalSuspensionType.MANUAL) {
					criteria.add(Restrictions.or(Restrictions.isNull("suspensionType"), 
							Restrictions.eq("suspensionType", type)));	
				} else {
					criteria.add(Restrictions.eq("suspensionType", type));	
				}
			}
		});
	}
	
	@Override
	@Transactional(readOnly = true)
	public Collection<PrincipalSuspension> getSuspensions(Principal principal) {
		return list("principal", principal, PrincipalSuspension.class);
	}
	
	@Override
	@Transactional
	public void deletePrincipalSuspension(PrincipalSuspension suspension) {
		delete(suspension);
	}

	@Override
	@Transactional
	public void saveSuspension(PrincipalSuspension principalSuspension) {
		save(principalSuspension);
	}
	
}
