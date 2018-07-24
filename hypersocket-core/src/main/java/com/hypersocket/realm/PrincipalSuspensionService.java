package com.hypersocket.realm;

import java.util.Collection;
import java.util.Date;

import com.hypersocket.resource.ResourceException;

public interface PrincipalSuspensionService {

	PrincipalSuspension createPrincipalSuspension(Principal principal, String username, Realm realm,
			Date startDate, Long duration, PrincipalSuspensionType type) throws ResourceException;

	PrincipalSuspension deletePrincipalSuspension(Principal principal, PrincipalSuspensionType type);

	public void notifyResume(String scheduleId, String name, boolean onSchedule);

	PrincipalSuspension getSuspension(String username, Realm realm, PrincipalSuspensionType type);

	Collection<PrincipalSuspension> getSuspensions(String username, Realm realm);

}
