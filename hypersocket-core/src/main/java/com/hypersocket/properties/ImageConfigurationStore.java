package com.hypersocket.properties;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ImageConfigurationStore implements XmlTemplatePropertyStore {

	static Logger log = Logger.getLogger(ImageConfigurationStore.class);

	File imageResources;

	Map<String,PropertyTemplate> templates = new HashMap<String,PropertyTemplate>();
	Map<String,List<PropertyTemplate>> templatesByModule = new HashMap<String,List<PropertyTemplate>>();
	
	public ImageConfigurationStore() {
		
	}

	@Override
	public String getPropertyValue(PropertyTemplate template) {
		File val = new File(imageResources, template.getResourceKey());
		if (!val.exists()) {
			return null;
		}
		return createURL(template.getResourceKey());
	}

	@Override
	public void setProperty(PropertyTemplate template, String value) {

		File val = new File(imageResources, template.getResourceKey());

		FileOutputStream out;
		try {
			out = new FileOutputStream(val);
			IOUtils.copy(
					new ByteArrayInputStream(Base64.decodeBase64(value
							.getBytes("UTF-8"))), out);
		} catch (Exception e) {
			log.error(
					"Failed to write image for resource "
							+ template.getResourceKey(), e);
			throw new IllegalStateException("");
		} 

	}

	@Override
	public List<Property> getProperties(String module) {
		
		List<Property> props = new ArrayList<Property>();
		for(PropertyTemplate template : templatesByModule.get(module)) {
			props.add(new ImageProperty(template.getResourceKey(), createURL(template.getResourceKey())));
		}
		return props;
	}
	
	private String createURL(String resourceKey) {
		return "/hypersocket/api/images/" + resourceKey;
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

	@Override
	public void init(Element element) throws IOException {

		NodeList filename = element.getElementsByTagName("filename");
		if(filename.getLength() != 1) {
			throw new IOException("<propertyStore> of type ImageConfigurationStore requires a child <filename> element");
		}

		imageResources = new File(filename.item(0).getTextContent().replace("${hypersocket.conf}", System.getProperty("hypersocket.conf", "conf")));
		imageResources.mkdirs();
	}

}
