package com.hypersocket.realm;

import java.util.Date;

import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.resource.ResourceNotFoundException;

public interface PrincipalSuspensionService {

	PrincipalSuspension createPrincipalSuspension(Principal principal,
			Date startDate, Long duration) throws ResourceNotFoundException,
			ResourceCreationException;

	PrincipalSuspension deletePrincipalSuspension(Principal principal);

	public void notifyResume(String name, boolean onSchedule);

	PrincipalSuspension getSuspension(Principal principal);

}
