package com.hypersocket.properties;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.http.client.utils.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.hypersocket.utils.FileUtils;

public class PropertiesFileConfigurationStore implements XmlTemplatePropertyStore {

	static Logger log = LoggerFactory.getLogger(PropertiesFileConfigurationStore.class);
	File propertiesFile;
	Properties properties;
	Map<String,PropertyTemplate> templates = new HashMap<String,PropertyTemplate>();
	Map<String,List<PropertyTemplate>> templatesByModule = new HashMap<String,List<PropertyTemplate>>();
	
	public PropertiesFileConfigurationStore() {
	}
	

	@Override
	public void init(Element element) throws IOException {
		
		NodeList filename = element.getElementsByTagName("filename");
		if(filename.getLength() != 1) {
			throw new IOException("<propertyStore> of type PropertiesFileConfigurationStore requires a child <filename> element");
		}
		
		this.propertiesFile = new File(filename.item(0).getTextContent().replace("${hypersocket.conf}", System.getProperty("hypersocket.conf", "conf")));

		properties = new Properties();

		FileInputStream in = new FileInputStream(propertiesFile);

		try {
			properties.load(in);
		} finally {
			FileUtils.closeQuietly(in);
		}
	}

	
	private void saveProperties() throws IOException {
		
		FileOutputStream out = new FileOutputStream(propertiesFile);
		
		try {
			properties.store(out, "Saved by Hypersocket on " + DateUtils.formatDate(new Date()));
		
		} finally {
			FileUtils.closeQuietly(out);
		}
	}

	@Override
	public void setProperty(PropertyTemplate template, String value) {
		
		if(Boolean.getBoolean("hypersocket.demo")) {
			throw new IllegalStateException("This is a demo. No changes to resources or settings can be persisted.");
		}
		properties.put(template.getResourceKey(), value);
		try {
			saveProperties();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to save property to properties file", e);
		}
	}

	@Override
	public String getPropertyValue(PropertyTemplate template) {
		if(properties.containsKey(template.getResourceKey())) {
			return properties.getProperty(template.getResourceKey());
		} else {
			return template.getDefaultValue();
		}
	}

	@Override
	public void registerTemplate(PropertyTemplate template, String module) {
		templates.put(template.getResourceKey(), template);
		if(!templatesByModule.containsKey(module)) {
			templatesByModule.put(module, new ArrayList<PropertyTemplate>());
		}
		templatesByModule.get(module).add(template);
	}


	@Override
	public PropertyTemplate getPropertyTemplate(String resourceKey) {
		return templates.get(resourceKey);
	}
}
