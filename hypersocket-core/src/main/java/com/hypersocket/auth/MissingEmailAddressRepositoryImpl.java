package com.hypersocket.auth;

import org.springframework.stereotype.Repository;

import com.hypersocket.auth.AuthenticationScheme;
import com.hypersocket.resource.AbstractResourceRepositoryImpl;

@Repository
public class MissingEmailAddressRepositoryImpl extends
		AbstractResourceRepositoryImpl<AuthenticationScheme> implements
		MissingEmailAddressRepository {

	@Override
	protected Class<AuthenticationScheme> getResourceClass() {
		return AuthenticationScheme.class;
	}
	
	
}
