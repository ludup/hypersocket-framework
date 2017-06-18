package com.hypersocket.alert;

import java.util.Date;

import com.hypersocket.properties.ResourceTemplateRepository;

public interface AlertKeyRepository extends
		ResourceTemplateRepository {

	long getKeyCount(String resourceKey, String key, Date since);

	void saveKey(AlertKey ak);

	void deleteKeys(String resourceKey, String key);

}
