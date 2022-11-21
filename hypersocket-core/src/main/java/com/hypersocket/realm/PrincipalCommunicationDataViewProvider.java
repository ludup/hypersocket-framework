package com.hypersocket.realm;

import java.util.Set;

import com.hypersocket.permissions.AccessDeniedException;

public interface PrincipalCommunicationDataViewProvider {

	Set<? extends CommunicationDataView> getPrincipalCommunicationDataView(Realm realm, Long id) throws AccessDeniedException;
}
