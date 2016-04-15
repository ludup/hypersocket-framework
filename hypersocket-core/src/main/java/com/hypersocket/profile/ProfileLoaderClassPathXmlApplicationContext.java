package com.hypersocket.profile;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.StandardEnvironment;

public class ProfileLoaderClassPathXmlApplicationContext extends ClassPathXmlApplicationContext {

	public ProfileLoaderClassPathXmlApplicationContext(String configLocation,String...profiles) {
		try{
			FieldUtils.writeField(this, "resourcePatternResolver", getResourcePatternResolver(),true);
			StandardEnvironment environment = new StandardEnvironment();
			environment.setActiveProfiles(profiles);
			setEnvironment(environment);
			setConfigLocations(new String[]{configLocation});
			refresh();
		}catch(IllegalAccessException e){
			throw new IllegalStateException(e);
		}
	}
}
