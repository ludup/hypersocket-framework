package com.hypersocket.tasks.alert;

import java.util.Date;

import com.hypersocket.properties.ResourceTemplateRepository;
import com.hypersocket.tasks.Task;

public interface AlertTaskRepository extends
		ResourceTemplateRepository {

	long getKeyCount(Task task, String key, Date since);

	void saveKey(AlertKey ak);

	void deleteKeys(Task task, String key);

}
