package com.hypersocket.account.linking;

import com.hypersocket.realm.Principal;
import com.hypersocket.repository.AbstractRepository;

public interface PrincipalRepository extends AbstractRepository<Long> {

	void savePrincipal(Principal secondary);

}
