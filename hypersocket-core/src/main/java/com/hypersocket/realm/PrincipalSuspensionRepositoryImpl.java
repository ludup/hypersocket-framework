package com.hypersocket.realm;

import java.util.Collection;
import java.util.List;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.repository.AbstractRepositoryImpl;

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

	@Override
	@Transactional(readOnly=true)
	public List<PrincipalSuspension> getSuspensions() {
		return (List<PrincipalSuspension>) list(PrincipalSuspension.class);
	}
	
	
}
