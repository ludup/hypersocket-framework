package com.hypersocket.account.linking;

import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.realm.Principal;
import com.hypersocket.repository.AbstractRepositoryImpl;

public class PrincipalRepositoryImpl extends AbstractRepositoryImpl<Long> implements PrincipalRepository {

	@Override
	@Transactional
	public void savePrincipal(Principal principal) {
		save(principal);
	}

}
