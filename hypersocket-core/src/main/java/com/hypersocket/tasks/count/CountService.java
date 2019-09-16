package com.hypersocket.tasks.count;

import java.util.Collection;

import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceException;

public interface CountService {

	void adjustCount(Realm realm, String resourceKey, Long amount) throws ResourceException;

	Long sum(Realm realm, boolean reset, String... resourceKeys) throws ResourceException;

	void resetKeys(Realm realm, Collection<String> keys) throws ResourceException;

}
