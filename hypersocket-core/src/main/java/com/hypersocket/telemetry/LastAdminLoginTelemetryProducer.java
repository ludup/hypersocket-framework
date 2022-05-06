package com.hypersocket.telemetry;

import java.util.Date;
import java.util.Iterator;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.UserPrincipal;
import com.hypersocket.resource.ResourceException;

@Component
public class LastAdminLoginTelemetryProducer implements TelemetryProducer {

	@Autowired
	private TelemetryService telemetryService;

	@Autowired
	private PermissionService permissionService;

	@Autowired
	private RealmService realmService;

	public LastAdminLoginTelemetryProducer() {
	}

	@PostConstruct
	private void setup() {
		telemetryService.registerProducer(this);
	}

	@Override
	public void fillRealm(Realm realm, JsonObject data) throws ResourceException, AccessDeniedException {
		Iterator<Principal> it = permissionService.iteratePrincipalsByRole(realmService.getSystemRealm(),
				permissionService.getSystemAdministratorRole()); 
		iterateAndAdd(data, it);
	}

	@Override
	public void fill(JsonObject data) throws ResourceException, AccessDeniedException {
		Iterator<Principal> it = permissionService.iteratePrincipalsByRole(realmService.getSystemRealm(),
				permissionService.getSystemAdministratorRole());
		iterateAndAdd(data, it);
	}

	protected void iterateAndAdd(JsonObject data, Iterator<Principal> it) {
		Date lastAdminLogin = null;
		while(it.hasNext()) {
			UserPrincipal<?> p = (UserPrincipal<?>) it.next();
			if(p.getLastSignOn() != null && (lastAdminLogin == null || p.getLastSignOn().after(lastAdminLogin))) {
				lastAdminLogin = p.getLastSignOn();
			}
		}
		if(lastAdminLogin != null) {
			data.addProperty("lastAdminSignOn", TelemetryProducer.formatAsUTC(lastAdminLogin));
		}
	}

}
