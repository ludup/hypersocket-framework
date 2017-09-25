package com.hypersocket.properties;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.hypersocket.ApplicationContextServiceImpl;
import com.hypersocket.encrypt.EncryptionService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.SimpleResource;
import com.hypersocket.resource.FindableResourceRepository;
import com.hypersocket.resource.RealmResource;
import com.hypersocket.resource.Resource;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.utils.HypersocketUtils;

public class EntityResourcePropertyStore extends AbstractResourcePropertyStore {

	static Logger log = LoggerFactory.getLogger(EntityResourcePropertyStore.class);

	static Map<Class<?>,FindableResourceRepository<?>> findableResourceRepositories = new HashMap<Class<?>,FindableResourceRepository<?>>();
	Map<String,EntityStoreRepository<?>> attributeRepositories = new HashMap<String,EntityStoreRepository<?>>();
	
	Map<Class<?>, PrimitiveParser<?>> primitiveParsers = new HashMap<Class<?>,PrimitiveParser<?>>();
	StringValue stringParser = new StringValue();
	EntityStoreRepository<?> attributeRepository;
	String attributeField;
	String name;
	
	public EntityResourcePropertyStore(EncryptionService encryptionService, String name) {
		
		this.name = name;
		primitiveParsers.put(Boolean.class, new BooleanValue());
		primitiveParsers.put(Integer.class, new IntegerValue());
		primitiveParsers.put(Long.class, new LongValue());
		primitiveParsers.put(Double.class, new DoubleValue());
		primitiveParsers.put(Date.class, new DateValue());
		primitiveParsers.put(int.class, new IntegerValue());
		primitiveParsers.put(long.class, new LongValue());
		
		setEncryptionService(encryptionService);
	}
	
	public static Collection<FindableResourceRepository<?>> getRepositories() {
		return Collections.unmodifiableCollection(findableResourceRepositories.values());
	}
	
	protected String getCacheName() {
		return name;
	}
	public boolean isDefaultStore() {
		return false;
	}
	
	public static void registerResourceService(Class<?> clz, 
			FindableResourceRepository<?> repository) {
		findableResourceRepositories.put(clz, repository);
	}
	
	public FindableResourceRepository<?> getRepository(Class<?> clz) {
		return findableResourceRepositories.get(clz);
	}
	
	public void registerAttributeRepository(String attributeBean, EntityStoreRepository<?> repository) {
		attributeRepositories.put(attributeBean, repository);
	}
	
	@Override
	protected String lookupPropertyValue(PropertyTemplate template) {
		return template.getDefaultValue();
	}

	@Override
	protected void doSetProperty(PropertyTemplate template, String value) {
		throw new UnsupportedOperationException("Entity resource property store requires an entity resource to set property value");
	}
	
	private Object resolveTargetEntity(SimpleResource resource, AbstractPropertyTemplate template) {
		if(template.getAttributes().containsKey("via")) {
			return getAttributeEntity(resource, template);
		}
		return resource;
	}
	
	private Object getAttributeEntity(SimpleResource resource, AbstractPropertyTemplate template) {
		try {
			String attributeField = template.getAttributes().get("via");
			EntityStoreRepository<?> attributeRepository = attributeRepositories.get(attributeField);
			String methodName = "get" + StringUtils.capitalize(attributeField);
			Method m = resource.getClass().getMethod(methodName, (Class<?>[])null);
			Object result =  m.invoke(resource);
			if(result==null) {
				methodName = "set" + StringUtils.capitalize(attributeField);
				m = resource.getClass().getMethod(methodName, m.getReturnType());
				try {
					m.invoke(resource, result = attributeRepository.getResourceById(resource.getId()));
					if(result==null) {
						m.invoke(resource, result = attributeRepository.createEntity(resource));
					}
				} catch (ResourceNotFoundException e) {
					m.invoke(resource, result = attributeRepository.createEntity(resource));
				}
			}
			if(result==null) {
				throw new IllegalStateException(String.format("Cannot resolve property %s", template.getResourceKey()));
			}
			return result;
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | AccessDeniedException e) {
			throw new IllegalStateException(String.format("Cannot resolve property %s", template.getResourceKey()));
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected String lookupPropertyValue(AbstractPropertyTemplate template,
			SimpleResource entity) {
		
		if(entity==null) {
			return template.getDefaultValue();
		}
		
		Object resource = resolveTargetEntity(entity, template);
		Throwable t;
		String methodName = "get" + StringUtils.capitalize(template.hasMapping() ? template.getMapping() : template.getResourceKey());
		try {
			
			Method m = resource.getClass().getMethod(methodName, (Class<?>[])null);
			
			if(findableResourceRepositories.containsKey(m.getReturnType())) {
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
					String inputType = template.getAttributes().get("inputType");
					if(inputType!=null && inputType.equals("multipleSearchInput")) {
						return ResourceUtils.implodeNamePairValues((Collection<? extends Resource>) m.invoke(resource));
					}
					return ResourceUtils.implodeResourceValues((Collection<? extends Resource>) m.invoke(resource));
				} else {
					throw new IllegalStateException("Unhandled Collection type for " + template.getResourceKey() + "! Type " + m.getReturnType() + " is not an enum, nor is it assignable as a Resource.");
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
			SimpleResource resource, String value) {
		doSetProperty(template, resource, value);
	}
	
	public void setPropertyValue(AbstractPropertyTemplate template, SimpleResource resource, String value) {
		
		// Prevent caching until resource has an id.
		if(resource.getId()==null) {
			doSetProperty(template, resource, value);
		} else {
			super.setPropertyValue(template, resource, value);
		}
	}
	
	@Override
	protected void doSetProperty(AbstractPropertyTemplate template,
			SimpleResource entity, String value) {
		
		if(value==null) {
			// We don't support setting null values. Caller may have set values on object directly
			return;
		}
		
		Object resource = resolveTargetEntity(entity, template);
		Method[] methods = resource.getClass().getMethods();
		
		String methodName = "set" + StringUtils.capitalize(template.hasMapping() ? template.getMapping() : template.getResourceKey());
		for(Method m : methods) {
			if(m.getName().equals(methodName)) {
				Class<?> clz = m.getParameterTypes()[0];
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
				} else if(String.class.equals(clz)) {
					try {
						m.invoke(resource, stringParser.parseValue(value, resource, template, entity.getUUID()));
					} catch (Exception e) {
						log.error("Could not set " + template.getResourceKey() + " String value " + value + " for resource " + resource.getClass().getName(), e);
						throw new IllegalStateException("Could not set " + template.getResourceKey() + " String value " + value + " for resource " + resource.getClass().getName(), e);
					}
					return;
				} else if (primitiveParsers.containsKey(clz)) {
					try {
						m.invoke(resource, StringUtils.isBlank(value) ? null : primitiveParsers.get(clz).parseValue(value));
					} catch (Exception e) {
						log.error("Could not set " + template.getResourceKey() + " primitive value " + value + " for resource " + resource.getClass().getName(), e);
						throw new IllegalStateException("Could not set " + template.getResourceKey() + " primitive value " + value + " for resource " + resource.getClass().getName(), e);
					}
					return;
				} else if(findableResourceRepositories.containsKey(clz)) {
					try {
						if(StringUtils.isEmpty(value)) {
							m.invoke(resource, (Object)null);
						} else {
							m.invoke(resource, findableResourceRepositories.get(clz).getResourceById(Long.parseLong(value)));
						}
					} catch (Exception e) {
						log.error("Could not lookup " + template.getResourceKey() + " value " + value + " for resource " + resource.getClass().getName(), e);
						throw new IllegalStateException("Could not lookup " + template.getResourceKey() + " value " + value + " for resource " + resource.getClass().getName(), e);
					}
					return;
				} else if(Collection.class.isAssignableFrom(clz)) {
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
					}  else if(findableResourceRepositories.containsKey(type)) {
						try {
							Collection<Object> values = m.getParameterTypes()[0].equals(List.class) ? new ArrayList<>() : new HashSet<>();
							if(StringUtils.isEmpty(value)) {
								m.invoke(resource, values);
							} else {
								String[] ids = ResourceUtils.explodeValues(value);
								for(String id : ids) {
									if(ResourceUtils.isNamePair(id)) {
										id = ResourceUtils.getNamePairKey(id);
									}
									Object obj = null;
									if(findableResourceRepositories.containsKey(type)) {
										obj = findableResourceRepositories.get(type).getResourceById(Long.parseLong(id));
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
						throw new IllegalStateException("Unhandled collection type! " + clz.getCanonicalName());
					}
				} else {
					throw new IllegalStateException("Unhandled parameter type! " + clz.getCanonicalName());
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
	
	class StringValue {
		public String parseValue(String value, Object resource, AbstractPropertyTemplate template, String uuid) {
			Realm realm = ApplicationContextServiceImpl.getInstance().getBean(RealmService.class).getSystemRealm();
			if(resource instanceof RealmResource) {
				realm = ((RealmResource)resource).getRealm();
			}
			if(template.isEncrypted()) {
				if(!ResourceUtils.isEncrypted(value)) {
					try {
						value = encryptionService.encryptString(uuid, value, realm);
					} catch (IOException e) {
						return value;
					}
				}
			}
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
			SimpleResource resource) {

		Object entity = resolveTargetEntity(resource, template);
		String methodName = "get" + StringUtils.capitalize(template.getResourceKey());
		try {
			
			entity.getClass().getMethod(methodName, (Class<?>[])null);
			return true;
			
		} catch (NoSuchMethodException e) {
		} catch (SecurityException e) {
		} catch (IllegalArgumentException e) {
		}
		return false;
	}
}
