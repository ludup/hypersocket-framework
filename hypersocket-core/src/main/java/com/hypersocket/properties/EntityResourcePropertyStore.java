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
			
			Method m = resource.getClass().getMethod(methodName, (Class<?>[])null);
			Object obj = m.invoke(resource);
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
		
		if(!doSetProperty(template, resource, String.class, new StringValue(value))) {
			if(!doSetProperty(template, resource, Boolean.class, new BooleanValue(value))) {
				if(!doSetProperty(template, resource, Integer.class, new IntegerValue(value))) {
					if(!doSetProperty(template, resource, Long.class, new LongValue(value))) {
						throw new IllegalStateException("set" + StringUtils.capitalize(template.getResourceKey()) + " not found"); 
					}
				}
			}
		}
	}
	
	private <T> boolean doSetProperty(AbstractPropertyTemplate template,
			AbstractResource resource, Class<T> clz, ReturnValue<T> value) {
		
		String methodName = "set" + StringUtils.capitalize(template.getResourceKey());
		try {
			Method m = resource.getClass().getMethod(methodName, new Class<?>[]{ clz });
			m.invoke(resource, value.getValue());
			return true;
		} catch (Throwable e) {
			return false;
		}
	}
	
	interface ReturnValue<T> {
		T getValue();
	}
	
	class StringValue implements ReturnValue<String> {
		
		String value;
		StringValue(String value) {
			this.value = value;
		}
		public String getValue() {
			return value;
		}
	}
	
	class BooleanValue implements ReturnValue<Boolean> {
		
		String value;
		BooleanValue(String value) {
			this.value = value;
		}
		public Boolean getValue() {
			return Boolean.valueOf(value);
		}
	}
	
	class IntegerValue implements ReturnValue<Integer> {
		
		String value;
		IntegerValue(String value) {
			this.value = value;
		}
		public Integer getValue() {
			return Integer.valueOf(value);
		}
	}
	
	class LongValue implements ReturnValue<Long> {
		
		String value;
		LongValue(String value) {
			this.value = value;
		}
		public Long getValue() {
			return Long.valueOf(value);
		}
	}

}
