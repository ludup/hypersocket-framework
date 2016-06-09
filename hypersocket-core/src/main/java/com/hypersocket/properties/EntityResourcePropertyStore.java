package com.hypersocket.properties;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.hypersocket.encrypt.EncryptionService;
import com.hypersocket.resource.AbstractAssignableResourceRepository;
import com.hypersocket.resource.AbstractResource;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.Resource;
import com.hypersocket.utils.HypersocketUtils;

public class EntityResourcePropertyStore extends AbstractResourcePropertyStore {

	static Logger log = LoggerFactory.getLogger(EntityResourcePropertyStore.class);
	
	static Map<Class<?>,AbstractResourceRepository<?>> resourceServices = new HashMap<Class<?>,AbstractResourceRepository<?>>();
	static Map<Class<?>,AbstractAssignableResourceRepository<?>> assignableServices = new HashMap<Class<?>,AbstractAssignableResourceRepository<?>>();
	Map<Class<?>, PrimitiveParser<?>> primitiveParsers = new HashMap<Class<?>,PrimitiveParser<?>>();
	
	public EntityResourcePropertyStore(EncryptionService encryptionService) {
		
		primitiveParsers.put(String.class, new StringValue());
		primitiveParsers.put(Boolean.class, new BooleanValue());
		primitiveParsers.put(Integer.class, new IntegerValue());
		primitiveParsers.put(Long.class, new LongValue());
		primitiveParsers.put(Double.class, new DoubleValue());
		primitiveParsers.put(Date.class, new DateValue());
		
		setEncryptionService(encryptionService);
	}
	
	public boolean isDefaultStore() {
		return false;
	}
	
	public void registerResourceService(Class<?> clz, AbstractResourceRepository<?> service) {
		resourceServices.put(clz, service);
	}
	
	public void registerResourceService(Class<?> clz, AbstractAssignableResourceRepository<?> service) {
		assignableServices.put(clz, service);
	}
	
	@Override
	protected String lookupPropertyValue(PropertyTemplate template) {
		return template.getDefaultValue();
	}

	@Override
	protected void doSetProperty(PropertyTemplate template, String value) {
		throw new UnsupportedOperationException("Entity resource property store requires an entity resource to set property value");
	}

	@SuppressWarnings("unchecked")
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
			if(assignableServices.containsKey(m.getReturnType()) || resourceServices.containsKey(m.getReturnType())) {
				Resource r = (Resource) m.invoke(resource);
				if(r==null) {
					return null;
				} else {
					return r.getId().toString();
				}
			} else if(Collection.class.isAssignableFrom(m.getReturnType())) { 
				Class<?> type = (Class<?>) ((ParameterizedType)m.getGenericReturnType()).getActualTypeArguments()[0];
				if(type.isEnum()) {
					return ResourceUtils.implodeEnumValues((Collection<Enum<?>>) m.invoke(resource));
				} else if(Resource.class.isAssignableFrom(type)) {
					return ResourceUtils.implodeResourceValues((Collection<? extends Resource>) m.invoke(resource));
				} else {
					throw new IllegalStateException("Unhandled Collection type!");
				}
			} else {
				Object obj = m.invoke(resource);
				if(obj==null) {
					return "";
				}
				return obj.toString();
			}
			
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

	public void setField(AbstractPropertyTemplate template,
			AbstractResource resource, String value) {
		doSetProperty(template, resource, value);
	}
	
	public void setPropertyValue(AbstractPropertyTemplate template, AbstractResource resource, String value) {
		
		// Prevent caching until resource has an id.
		if(resource.getId()==null) {
			doSetProperty(template, resource, value);
		} else {
			super.setPropertyValue(template, resource, value);
		}
	}
	
	@Override
	protected void doSetProperty(AbstractPropertyTemplate template,
			AbstractResource resource, String value) {
		
		if(value==null) {
			// We don't support setting null values. Caller may have set values on object directly
			return;
		}
		Method[] methods = resource.getClass().getMethods();
		
		String methodName = "set" + StringUtils.capitalize(template.getResourceKey());
		for(Method m : methods) {
			if(m.getName().equals(methodName)) {
				Class<?> clz = m.getParameterTypes()[0];
				Type clzType = (Type)clz;
				if(clz.isEnum()){
					try {
						Enum<?>[] enumConstants = (Enum<?>[]) resource.getClass().getDeclaredField(template.getResourceKey()).getType().getEnumConstants();
						if(NumberUtils.isNumber(value)){//ordinal
							Enum<?> enumConstant = enumConstants[Integer.parseInt(value)];
							m.invoke(resource, enumConstant);
							return;  
						}else{//name
							for (Enum<?> enumConstant : enumConstants) {
								if(enumConstant.name().equals(value)){
									m.invoke(resource, enumConstant);
									return;
								}
							}
						}
						throw new IllegalArgumentException(String.format("Matching enum value could not be fetched for value %s", value));
						
					} catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
						log.error("Could not set " + template.getResourceKey() + " enum value " + value + " for resource " + resource.getClass().getName(), e);
						throw new IllegalStateException("Could not set " + template.getResourceKey() + " enum value " + value + " for resource " + resource.getClass().getName(), e);
					}
				}
				if(primitiveParsers.containsKey(clz)) {
					try {
						m.invoke(resource, primitiveParsers.get(clz).parseValue(value));
					} catch (Exception e) {
						log.error("Could not set " + template.getResourceKey() + " primitive value " + value + " for resource " + resource.getClass().getName(), e);
						throw new IllegalStateException("Could not set " + template.getResourceKey() + " primitive value " + value + " for resource " + resource.getClass().getName(), e);
					}
					return;
				}
				if(resourceServices.containsKey(clz)) {
					try {
						if(StringUtils.isEmpty(value)) {
							m.invoke(resource, (Object)null);
						} else {
							m.invoke(resource, resourceServices.get(clz).getResourceById(Long.parseLong(value)));
						}
					} catch (Exception e) {
						log.error("Could not lookup " + template.getResourceKey() + " value " + value + " for resource " + resource.getClass().getName(), e);
						throw new IllegalStateException("Could not lookup " + template.getResourceKey() + " value " + value + " for resource " + resource.getClass().getName(), e);
					}
					return;
				}
				if(assignableServices.containsKey(clz)) {
					try {
						if(StringUtils.isEmpty(value)) {
							m.invoke(resource, (Object)null);
						} else {
							m.invoke(resource, assignableServices.get(clz).getResourceById(Long.parseLong(value)));
						}
					} catch (Exception e) {
						log.error("Could not lookup " + template.getResourceKey() + " value " + value + " for resource " + resource.getClass().getName(), e);
						throw new IllegalStateException("Could not lookup " + template.getResourceKey() + " value " + value + " for resource " + resource.getClass().getName(), e);
					}
					return;
				}
				if(Collection.class.isAssignableFrom(clz)) {
					// We have a collection of entity values
					
					Class<?> type = (Class<?>) ((ParameterizedType) m.getGenericParameterTypes()[0]).getActualTypeArguments()[0];
					if(type.isEnum()) {
						try {
							Collection<Object> values = new ArrayList<Object>();
							if(!StringUtils.isEmpty(value)) {
								String[] ids = ResourceUtils.explodeValues(value);
								for(String id : ids) {
									values.add(getEnum(id, type));
								}
							}
							m.invoke(resource, values);
							return;
						} catch (Exception e) {
							log.error("Could not lookup " + template.getResourceKey() + " value " + value + " for resource " + resource.getClass().getName(), e);
							throw new IllegalStateException("Could not lookup " + template.getResourceKey() + " value " + value + " for resource " + resource.getClass().getName(), e);
						}
					}  else if(Resource.class.isAssignableFrom(type)) {
						try {
							Collection<Object> values = new ArrayList<Object>();
							if(StringUtils.isEmpty(value)) {
								m.invoke(resource, values);
							} else {
								String[] ids = ResourceUtils.explodeValues(value);
								for(String id : ids) {
									Object obj = null;
									if(assignableServices.containsKey(type)) {
										obj = assignableServices.get(type).getResourceById(Long.parseLong(id));
									} else if(resourceServices.containsKey(type)) {
										obj = resourceServices.get(type).getResourceById(Long.parseLong(id));
									} else {
										throw new IllegalStateException(String.format
												("Collection type %s does not appear to be registered with entity store", 
												type.getSimpleName()));
									}
									values.add(obj);
								}
								m.invoke(resource, values);
							}
						} catch (Exception e) {
							log.error("Could not lookup " + template.getResourceKey() + " value " + value + " for resource " + resource.getClass().getName(), e);
							throw new IllegalStateException("Could not lookup " + template.getResourceKey() + " value " + value + " for resource " + resource.getClass().getName(), e);
						}
						return;
					} else {
						throw new IllegalStateException("Unhandled Collection type!");
					}
				}
				
			}
		}
		
		if(log.isDebugEnabled()) {
			log.debug(template.getResourceKey() + " is not a property of the entity " + resource.getClass().getName());
		}
	}

	@SuppressWarnings("unchecked")
	private <T> T getEnum(String value, Class<T> enumType) {
		Enum<?>[] enumConstants = (Enum<?>[]) enumType.getEnumConstants();
		if(NumberUtils.isNumber(value)){//ordinal
			return (T) enumConstants[Integer.parseInt(value)];
		}else{
			for (Enum<?> enumConstant : enumConstants) {
				if(enumConstant.name().equals(value)){
					return (T) enumConstant;
				}
			}
		}
		throw new IllegalStateException(String.format("Cannot find enum value %s", value));
	}
	interface PrimitiveParser<T> {
		T parseValue(String value);
	}
	
	class StringValue implements PrimitiveParser<String> {
		public String parseValue(String value) {
			return value;
		}
	}
	
	class BooleanValue implements PrimitiveParser<Boolean> {
		public Boolean parseValue(String value) {
			if(value == null) {
				return Boolean.FALSE;
			}
			return Boolean.valueOf(value);
		}
	}
	
	class IntegerValue implements PrimitiveParser<Integer> {
		public Integer parseValue(String value) {
			if(value == null) {
				return new Integer(0);
			}
			return Integer.valueOf(value);
		}
	}
	
	class LongValue implements PrimitiveParser<Long> {
		public Long parseValue(String value) {
			if(value == null) {
				return new Long(0);
			}
			return Long.valueOf(value);
		}
	}
	
	class DoubleValue implements PrimitiveParser<Double> {
		public Double parseValue(String value) {
			if(value == null) {
				return new Double(0F);
			}
			return Double.valueOf(value);
		}
	}
	
	class DateValue implements PrimitiveParser<Date> {

		@Override
		public Date parseValue(String value) {
			try {
				if(value == null) {
					return new Date();
				}
				return HypersocketUtils.parseDate(value, "yyyy-MM-dd");
			} catch (ParseException e) {
				if(StringUtils.isNotBlank(value)) {
					log.warn("Failed to parse date value '" + value + "'");
				}
				return null;
			}
		}
		
	}
	
	@Override
	public void init(Element element) throws IOException {
		
	}

	@Override
	public boolean hasPropertyValueSet(AbstractPropertyTemplate template,
			AbstractResource resource) {

		String methodName = "get" + StringUtils.capitalize(template.getResourceKey());
		try {
			
			resource.getClass().getMethod(methodName, (Class<?>[])null);
			return true;
			
		} catch (NoSuchMethodException e) {
		} catch (SecurityException e) {
		} catch (IllegalArgumentException e) {
		}
		return false;
	}

}
