package com.hypersocket.config;

import java.util.Map;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.hypersocket.properties.ResourceTemplateRepositoryImpl;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.SimpleResource;

@Repository
public class ConfigurationRepositoryImpl extends ResourceTemplateRepositoryImpl implements ConfigurationRepository {

	@Override
	@Transactional
	public void setValues(SimpleResource resource, Map<String, String> values) {
		
		for(Map.Entry<String,String> e : values.entrySet()) {
			setValue(resource, e.getKey(), e.getValue());
		}
	}

	@Override
	public void deleteRealm(Realm realm) {
		/** TODO **/
	}

}
