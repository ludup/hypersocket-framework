package com.hypersocket.properties;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.hypersocket.resource.AbstractResource;
import com.hypersocket.utils.HypersocketUtils;

public class ImageConfigurationStore implements ResourcePropertyStore {

	static Logger log = Logger.getLogger(ImageConfigurationStore.class);

	File imageResources;

	Map<String,PropertyTemplate> templates = new HashMap<String,PropertyTemplate>();
	Map<String,List<PropertyTemplate>> templatesByModule = new HashMap<String,List<PropertyTemplate>>();
	
	Properties resourceKeyImages;
	File resourceKeyImagesFile;
	String id;
	
	public ImageConfigurationStore() {
		
	}

	public boolean isDefaultStore() {
		return false;
	}
	
	@Override
	public String getPropertyValue(PropertyTemplate template) {
		if(!resourceKeyImages.containsKey(template.getResourceKey())) {
			return null;
		}
		File val = new File(imageResources, resourceKeyImages.getProperty(template.getResourceKey()));
		if (!val.exists()) {
			return null;
		}
		return createURL(resourceKeyImages.getProperty(template.getResourceKey()));
	}

	@Override
	public void setProperty(PropertyTemplate template, String value) {

		String filename = template.getResourceKey();
		int idx;
		if((idx = value.indexOf(';')) > -1) {
			filename = value.substring(0, idx);
			value = value.substring(idx+1);
		}
		
		resourceKeyImages.put(template.getResourceKey(), filename);
		try {
			resourceKeyImages.store(new FileOutputStream(resourceKeyImagesFile), "");
		} catch (IOException e1) {
			throw new IllegalStateException("Failed to save image.properties", e1);
		}
		
		File val = new File(imageResources, filename);

		FileOutputStream out;
		try {
			out = new FileOutputStream(val);
			IOUtils.copy(
					new ByteArrayInputStream(HypersocketUtils.base64Decode(value)), out);
		} catch (Exception e) {
			log.error(
					"Failed to write image for resource "
							+ template.getResourceKey(), e);
			throw new IllegalStateException("");
		} 

	}

//	@Override
//	public List<Property> getProperties(String module) {
//		
//		List<Property> props = new ArrayList<Property>();
//		for(PropertyTemplate template : templatesByModule.get(module)) {
//			props.add(new ImageProperty(template.getResourceKey(), createURL(resourceKeyImages.getProperty(template.getResourceKey()))));
//		}
//		return props;
//	}
//	
	private String createURL(String filename) {
		return System.getProperty("hypersocket.appPath", "/hypersocket") + "/"+ id + "/" + filename;
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

		id = element.getAttribute("id");
		
		imageResources = new File(filename.item(0).getTextContent().replace("${hypersocket.conf}", System.getProperty("hypersocket.conf", "conf")));
		imageResources.mkdirs();
		
		resourceKeyImages = new Properties();
		resourceKeyImagesFile = new File(imageResources, "image.properties");
		
		if(resourceKeyImagesFile.exists()) {
			InputStream in = new FileInputStream(resourceKeyImagesFile);
			try {
				resourceKeyImages.load(in);
			} finally {
				IOUtils.closeQuietly(in);
			}
		}
		
	}

	@Override
	public String getPropertyValue(AbstractPropertyTemplate template,
			AbstractResource resource) {
		
		String key = resource.getId() + File.separator + template.getResourceKey();
		
		if(!resourceKeyImages.containsKey(key)) {
			return null;
		}
		File val = new File(imageResources, resourceKeyImages.getProperty(key));
		if (!val.exists()) {
			return null;
		}
		return createURL(resourceKeyImages.getProperty(key));
	}

	@Override
	public void setPropertyValue(AbstractPropertyTemplate template,
			AbstractResource resource, String value) {
		
		String key = resource.getId() + File.separator + template.getResourceKey();
		String filename = key;
		int idx;
		if((idx = value.indexOf(';')) > -1) {
			filename = resource.getId() + File.separator + value.substring(0, idx);
			value = value.substring(idx+1);
		}
		
		resourceKeyImages.put(key, filename);
		try {
			resourceKeyImages.store(new FileOutputStream(resourceKeyImagesFile), "");
		} catch (IOException e1) {
			throw new IllegalStateException("Failed to save image.properties", e1);
		}
		
		File imageFile = new File(imageResources, filename);
		imageFile.getParentFile().mkdirs();
		
		FileOutputStream out;
		try {
			out = new FileOutputStream(imageFile);
			IOUtils.copy(
					new ByteArrayInputStream(HypersocketUtils.base64Decode(value)), out);
		} catch (Exception e) {
			log.error(
					"Failed to write image for resource "
							+ template.getResourceKey(), e);
			throw new IllegalStateException("");
		} 

		
	}

	@Override
	public Collection<String> getPropertyNames() {
		return templates.keySet();
	}

	@Override
	public boolean hasPropertyValueSet(AbstractPropertyTemplate template,
			AbstractResource resource) {
		
		String key = resource.getId() + File.separator + template.getResourceKey();
		return resourceKeyImages.containsKey(key);
	}

	@Override
	public String getDecryptedValue(AbstractPropertyTemplate template, AbstractResource resource) {
		throw new UnsupportedOperationException();
	}

}
