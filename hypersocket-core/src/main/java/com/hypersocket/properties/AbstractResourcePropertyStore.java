package com.hypersocket.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.encrypt.EncryptionService;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResource;
import com.hypersocket.resource.RealmResource;

public abstract class AbstractResourcePropertyStore implements ResourcePropertyStore {

	static Logger log = LoggerFactory.getLogger(AbstractResourcePropertyStore.class);
	
	Map<String, String> cachedValues = new HashMap<String, String>();
	Map<String, PropertyTemplate> templates = new HashMap<String, PropertyTemplate>();
	Map<String, List<PropertyTemplate>> templatesByModule = new HashMap<String, List<PropertyTemplate>>();

	EncryptionService encryptionService; 
	
	public AbstractResourcePropertyStore() {
	}
	
	protected void setEncryptionService(EncryptionService encryptionService) {
		this.encryptionService = encryptionService;
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
	
	@Override
	public Collection<String> getPropertyNames() {
		return templates.keySet();
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
	
	@Override
	public String getDecryptedValue(AbstractPropertyTemplate template, AbstractResource resource) {
		
		String c;
		String cacheKey = createCacheKey(template.getResourceKey(), resource);
		if (!cachedValues.containsKey(cacheKey)) {
			c = lookupPropertyValue(template, resource);
			cachedValues.put(cacheKey, c);
		} else {
			c = cachedValues.get(cacheKey);
		}

		if(template.isEncrypted() && c.startsWith("!ENC!")) {
			c = decryptValue(cacheKey, c, resolveRealm(resource));
		}

		return c;
	}
	
	protected abstract void doSetProperty(AbstractPropertyTemplate template, AbstractResource resource, String value);


	@Override
	public void setPropertyValue(AbstractPropertyTemplate template,
			AbstractResource resource, String value) {

		String cacheKey = createCacheKey(template.getResourceKey(), resource);
		cachedValues.remove(cacheKey);
		
		if(template.isEncrypted() && !value.startsWith("!ENC!")) {
			value = encryptValue(cacheKey, value, resolveRealm(resource));
			doSetProperty(template, resource, value);
		} else {
			doSetProperty(template, resource, value);
		}
		

	}
	
	private String encryptValue(String cacheKey, String value, Realm realm) {
		try {
			return "!ENC!" + encryptionService.encryptString(cacheKey, value, realm);
		} catch (Exception e) {
			throw new IllegalStateException("Could not encrypt property value. Check the logs for more detail.", e);
		}
	}
	
	private String decryptValue(String cacheKey, String value, Realm realm) {
		try {
			String e =  value.substring(5);
			return encryptionService.decryptString(cacheKey, e, realm);
		} catch(Exception e) {
			log.warn("Unable to decrypt " + cacheKey + "; returning encrypted", e);
			return value;
		}
	}
	
	
	private Realm resolveRealm(AbstractResource resource) {
		if(resource instanceof RealmResource) {
			return ((RealmResource) resource).getRealm();
		} else if(resource instanceof Realm) {
			return (Realm) resource;
		}
		
		throw new IllegalStateException("Use of ResourcePropertyStore requires Realm based resource, either Realm or extension of RealmResource");
	}

}
