package com.hypersocket.realm;

import java.util.Collection;
import java.util.Date;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceException;

public interface PrincipalSuspensionService {

	PrincipalSuspension createPrincipalSuspension(Principal principal, String username, Realm realm,
			Date startDate, Long duration, PrincipalSuspensionType type) throws ResourceException, AccessDeniedException;

	PrincipalSuspension deletePrincipalSuspension(Principal principal, PrincipalSuspensionType type) throws AccessDeniedException;

	public void notifyResume(String scheduleId, String name, boolean onSchedule);

	PrincipalSuspension getSuspension(String username, Realm realm, PrincipalSuspensionType type);

	Collection<PrincipalSuspension> getSuspensions(String username, Realm realm);

}
