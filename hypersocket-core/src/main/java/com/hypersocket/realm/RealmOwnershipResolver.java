package com.hypersocket.realm;

import java.util.Collection;

public interface RealmOwnershipResolver {

	Collection<Realm> resolveRealms(Principal principal);
}
