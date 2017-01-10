package com.hypersocket.realm;

import java.util.Collection;
import java.util.Date;

import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceNotFoundException;

public interface PrincipalSuspensionService {

	PrincipalSuspension createPrincipalSuspension(Principal principal,
			Date startDate, Long duration, PrincipalSuspensionType type) throws ResourceNotFoundException,
			ResourceCreationException;

	PrincipalSuspension deletePrincipalSuspension(Principal principal, PrincipalSuspensionType type);

	public void notifyResume(String name, boolean onSchedule);

	PrincipalSuspension getSuspension(Principal principal, PrincipalSuspensionType type);

	Collection<PrincipalSuspension> getSuspensions(Principal principal);

}
