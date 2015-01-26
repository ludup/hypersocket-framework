package com.hypersocket.properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.binary.Hex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.auth.PasswordEncryptionService;
import com.hypersocket.resource.AbstractResource;

public abstract class AbstractResourcePropertyStore implements ResourcePropertyStore {

	static Logger log = LoggerFactory.getLogger(AbstractResourcePropertyStore.class);
	
	Map<String, String> cachedValues = new HashMap<String, String>();
	Map<String, PropertyTemplate> templates = new HashMap<String, PropertyTemplate>();
	Map<String, List<PropertyTemplate>> templatesByModule = new HashMap<String, List<PropertyTemplate>>();

	@Autowired
	PasswordEncryptionService encryptionService; 
	
	public AbstractResourcePropertyStore() {
	}

	protected abstract String lookupPropertyValue(PropertyTemplate template);
	
	protected abstract void doSetProperty(PropertyTemplate template, String value);
	
	@Override
	public void setProperty(PropertyTemplate template, String value) {

		doSetProperty(template, value);
		cachedValues.put(template.getResourceKey(), value);
	}

	@Override
	public String getPropertyValue(PropertyTemplate template) {

		String c = null;
		if (!cachedValues.containsKey(template.getResourceKey())) {
			c = lookupPropertyValue(template);
			cachedValues.put(template.getResourceKey(), c);
		} else {
			c = cachedValues.get(template.getResourceKey());
		}

		return c;
	}

	private String createCacheKey(String resourceKey, AbstractResource resource) {
		String key = resourceKey;
		if (resource != null) {
			key += "/" + resource.getId();
		}
		return key;
	}

	@Override
	public void registerTemplate(PropertyTemplate template, String module) {
		templates.put(template.getResourceKey(), template);
		if (!templatesByModule.containsKey(module)) {
			templatesByModule.put(module, new ArrayList<PropertyTemplate>());
		}
		templatesByModule.get(module).add(template);
	}

	@Override
	public PropertyTemplate getPropertyTemplate(String resourceKey) {
		return templates.get(resourceKey);
	}

	protected abstract String lookupPropertyValue(AbstractPropertyTemplate template, AbstractResource resource);
	
	@Override
	public String getPropertyValue(AbstractPropertyTemplate template,
			AbstractResource resource) {
		String c;
		String cacheKey = createCacheKey(template.getResourceKey(), resource);
		if (!cachedValues.containsKey(cacheKey)) {
			c = lookupPropertyValue(template, resource);
			cachedValues.put(cacheKey, c);
		} else {
			c = cachedValues.get(cacheKey);
		}

		return c;
	}
	
	protected abstract void doSetProperty(AbstractPropertyTemplate template, AbstractResource resource, String value);

	@Override
	public void setPropertyValue(AbstractPropertyTemplate template,
			AbstractResource resource, String value) {

		if(template.isEncrypted()) {
			doSetProperty(template, resource, encryptValue(value));
		} else {
			doSetProperty(template, resource, value);
		}
		String cacheKey = createCacheKey(template.getResourceKey(), resource);
		cachedValues.remove(cacheKey);
		cachedValues.put(cacheKey, value);
	}
	
	private String encryptValue(String value) {
		return value;
//		try {
//		byte[] salt = encryptionService.generateSalt();
//		String encrypted = encryptionService.encrypt(value, salt);
//		
//		return "ENC" + Hex.encodeHexString(salt) + encrypted;
//		} catch(Exception ex) {
//			log.error("Failed to encrypt property value", ex);
//			return value;
//		}
	}

}
