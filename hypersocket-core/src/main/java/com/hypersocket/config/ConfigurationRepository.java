package com.hypersocket.config;

import java.util.Map;

import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.SimpleResource;

public interface ConfigurationRepository extends ResourceTemplateRepository {

	void setValues(SimpleResource resource, Map<String, String> values);

	void deleteRealm(Realm realm);
	

}
