package com.hypersocket.util;

import java.io.File;

import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;

public class OverridePropertyPlaceholderConfigurer extends
		PropertyPlaceholderConfigurer {

	static File override = null;
	public OverridePropertyPlaceholderConfigurer() {

	}
	
	public static void setOverrideFile(File override) {
		OverridePropertyPlaceholderConfigurer.override = override;
	}
	
	@Override
	public void setLocation(Resource location) {
		if(override==null) {
			super.setLocation(location);
		} else {
			super.setLocation(new FileSystemResource(override));
		}
	}
	@Override
	public void setLocations(Resource[] locations) {
		if(override==null) {
			super.setLocations(locations);
		} else {
			super.setLocations(new Resource[] {new FileSystemResource(override)});
		}
	}
	
}
