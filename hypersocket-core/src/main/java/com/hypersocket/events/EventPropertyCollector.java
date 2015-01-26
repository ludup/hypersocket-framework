package com.hypersocket.events;

import java.util.Set;

import com.hypersocket.realm.Realm;

public interface EventPropertyCollector {

	Set<String> getPropertyNames(String resourceKey, Realm realm);
}
