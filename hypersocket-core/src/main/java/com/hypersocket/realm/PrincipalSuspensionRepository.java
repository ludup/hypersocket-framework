package com.hypersocket.realm;

import java.util.Collection;

import com.hypersocket.repository.AbstractRepository;

public interface PrincipalSuspensionRepository extends
		AbstractRepository<Long> {

	void createPrincipalSuspension(PrincipalSuspension principalSuspension);
	
	void deletePrincipalSuspension(PrincipalSuspension suspension);

	void saveSuspension(PrincipalSuspension principalSuspension);

	Collection<PrincipalSuspension> getSuspensions(Principal principal, PrincipalSuspensionType type);
	
	Collection<PrincipalSuspension> getSuspensions(Principal principal);

}
