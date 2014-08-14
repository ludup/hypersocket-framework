package com.hypersocket.properties;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import com.hypersocket.resource.AbstractResource;

@Component
public class EntityResourcePropertyStore extends AbstractResourcePropertyStore {

	@Override
	protected String lookupPropertyValue(PropertyTemplate template) {
		throw new UnsupportedOperationException("Entity resource property store requires an entity resource to lookup property value");
	}

	@Override
	protected void doSetProperty(PropertyTemplate template, String value) {
		throw new UnsupportedOperationException("Entity resource property store requires an entity resource to set property value");
	}

	@Override
	protected String lookupPropertyValue(AbstractPropertyTemplate template,
			AbstractResource resource) {
		
		if(resource==null) {
			return template.getDefaultValue();
		}
		Throwable t;
		String methodName = "get" + StringUtils.capitalize(template.getResourceKey());
		try {
			
			Method m = resource.getClass().getMethod(methodName, null);
			Object obj = m.invoke(resource, null);
			if(obj==null) {
				return "";
			}
			return obj.toString();
		} catch (NoSuchMethodException e) {
			t = e;
		} catch (SecurityException e) {
			t = e;
		} catch (IllegalAccessException e) {
			t = e;
		} catch (IllegalArgumentException e) {
			t = e;
		} catch (InvocationTargetException e) {
			t = e;
		}
		throw new IllegalStateException(methodName + " not found", t);
	}

	@Override
	protected void doSetProperty(AbstractPropertyTemplate template,
			AbstractResource resource, String value) {
		
		Throwable t;
		String methodName = "set" + StringUtils.capitalize(template.getResourceKey());
		try {
			
			Method m = resource.getClass().getMethod(methodName, new Class<?>[]{ String.class });
			m.invoke(resource, value);
			return;
		} catch (NoSuchMethodException e) {
			t = e;
		} catch (SecurityException e) {
			t = e;
		} catch (IllegalAccessException e) {
			t = e;
		} catch (IllegalArgumentException e) {
			t = e;
		} catch (InvocationTargetException e) {
			t = e;
		}
		throw new IllegalStateException(methodName + " not found", t);

	}

}
