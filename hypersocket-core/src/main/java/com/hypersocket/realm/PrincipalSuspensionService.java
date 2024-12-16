package com.hypersocket.realm;

import java.util.Collection;
import java.util.Date;

import com.hypersocket.resource.ResourceException;

public interface PrincipalSuspensionService {

	PrincipalSuspension createPrincipalSuspension(Principal principal, Realm realm,
			Date startDate, Long duration, PrincipalSuspensionType type) throws ResourceException;

	PrincipalSuspension deletePrincipalSuspension(Principal principal, PrincipalSuspensionType type);

	public void notifyResume(Principal principal, Realm realm, boolean onSchedule);

	PrincipalSuspension getSuspension(Principal principal, Realm realm, PrincipalSuspensionType type);

	Collection<PrincipalSuspension> getSuspensions(Principal principal, Realm realm);

}
