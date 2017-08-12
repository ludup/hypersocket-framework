package com.hypersocket.realm;

import java.util.Collection;
import java.util.Date;

import com.hypersocket.resource.ResourceException;

public interface PrincipalSuspensionService {

	PrincipalSuspension createPrincipalSuspension(Principal principal,
			Date startDate, Long duration, PrincipalSuspensionType type) throws ResourceException;

	PrincipalSuspension deletePrincipalSuspension(Principal principal, PrincipalSuspensionType type);

	public void notifyResume(String scheduleId, String name, boolean onSchedule);

	PrincipalSuspension getSuspension(Principal principal, PrincipalSuspensionType type);

	Collection<PrincipalSuspension> getSuspensions(Principal principal);

}
