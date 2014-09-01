package com.hypersocket.triggers.actions.alert;

import java.util.Date;

import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.triggers.TriggerAction;

public interface AlertTriggerActionRepository extends
		ResourceTemplateRepository {

	long getKeyCount(TriggerAction action, String key, Date since);

	void saveKey(AlertKey ak);

	void deleteKeys(TriggerAction action, String key);

}
