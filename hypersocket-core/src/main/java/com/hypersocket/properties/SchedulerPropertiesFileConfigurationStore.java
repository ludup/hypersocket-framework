package com.hypersocket.properties;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class SchedulerPropertiesFileConfigurationStore extends PropertiesFileConfigurationStore {

	@Override
	protected Properties newPropertiesFile(Properties properties) {
		Properties p = new Properties();
		try(InputStream in = getClass().getResourceAsStream("/conf/quartz.properties")) {
			p.load(in);
			properties.putAll(p);
		}
		catch(IOException ioe) {
		}
		return properties;
	}

}
