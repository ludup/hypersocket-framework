package com.hypersocket.realm;

import java.util.Collection;
import java.util.List;

import com.hypersocket.repository.AbstractRepository;

public interface PrincipalSuspensionRepository extends
		AbstractRepository<Long> {

	void createPrincipalSuspension(PrincipalSuspension principalSuspension);
	
	void deletePrincipalSuspension(PrincipalSuspension suspension);

	void saveSuspension(PrincipalSuspension principalSuspension);

	List<PrincipalSuspension> getSuspensions();

	Collection<PrincipalSuspension> getSuspensions(Principal principal);

}
