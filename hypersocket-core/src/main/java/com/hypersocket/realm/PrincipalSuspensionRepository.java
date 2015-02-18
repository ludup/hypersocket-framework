package com.hypersocket.realm;

import java.util.List;

import com.hypersocket.repository.AbstractRepository;

public interface PrincipalSuspensionRepository extends
		AbstractRepository<Long> {

	void createPrincipalSuspension(PrincipalSuspension principalSuspension);

	PrincipalSuspension getSuspension(Principal principal);

	void deletePrincipalSuspension(PrincipalSuspension suspension);

	void saveSuspension(PrincipalSuspension principalSuspension);

	List<PrincipalSuspension> getSuspensions();

}
