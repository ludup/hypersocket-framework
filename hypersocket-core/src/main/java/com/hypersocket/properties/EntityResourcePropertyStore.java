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
import com.hypersocket.resource.FindableResourceRepository;
import com.hypersocket.resource.RealmResource;
import com.hypersocket.resource.Resource;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.resource.SimpleResource;
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
		primitiveParsers.put(boolean.class, new BooleanValue());
		
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
				throw new IllegalStateException(String.format("Cannot resolve property %s via %s", template.getResourceKey(), attributeField));
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
		
		if(entity==null || "true".equalsIgnoreCase(template.getAttributes().get("transient"))) {
			return template.getDefaultValue();
		}
		
		Object resource = resolveTargetEntity(entity, template);
		
		String fieldName = template.hasMapping() ? template.getMapping() : template.getResourceKey();
		while(fieldName.contains(".")) {
			try {
				String entityName = StringUtils.substringBefore(fieldName, ".");
				String entityGetMethod = "get" + StringUtils.capitalize(entityName);
				Method m = resource.getClass().getMethod(entityGetMethod, (Class<?>[])null);
				resource = m.invoke(resource, (Object[])null);
				if(resource==null) {
					return null;
				}
				fieldName = StringUtils.substringAfter(fieldName, ".");
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
		
		Throwable t;
		String methodName = "get" + StringUtils.capitalize(fieldName);
		try {
			
			Method m = null;
			try {
				m = resource.getClass().getMethod(methodName, (Class<?>[])null);
			}
			catch(NoSuchMethodException nsme) {
				/* Look for an is* method, but only use it if the return type is boolean and there are no parameters */
				String isName = "is" + StringUtils.capitalize(fieldName);
				try {
					Method is = resource.getClass().getMethod(isName, (Class<?>[])null);
					if(is.getParameterCount() == 0 && ( is.getReturnType().equals(boolean.class) || is.getReturnType().equals(Boolean.class))) {
						methodName = isName;
						m = is;
					}
				}
				catch(NoSuchMethodException nsme2) {
					/* Re-throw original error to keep existing behaviour */
					throw nsme;
				}
			}
			
			if(findableResourceRepositories.containsKey(m.getReturnType())) {
				Resource r = (Resource) m.invoke(resource);
				if(r==null || r.getId()==null) {
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
						return ResourceUtils.implodeNamePairValues(new NameValueImploder<Resource>() {

							@Override
							public String getId(Resource t) {
								if(template.getAttributes().containsKey("valueAttr")) {
									String nameMethod = "get" + StringUtils.capitalize(template.getAttributes().get("valueAttr"));
									try {
										Method method = t.getClass().getMethod(nameMethod, (Class<?>[])null);
										Object v = method.invoke(t);
										return v == null ? "" : v.toString();
									} catch (NoSuchMethodException | SecurityException | IllegalAccessException
											| IllegalArgumentException | InvocationTargetException e) {
									}
								} 
								return t.getId().toString();
								
							}

							@Override
							public String getName(Resource t) {
								if(template.getAttributes().containsKey("nameAttr")) {
									String nameMethod = "get" + StringUtils.capitalize(template.getAttributes().get("nameAttr"));
									try {
										Method method = t.getClass().getMethod(nameMethod, (Class<?>[])null);
										Object v = method.invoke(t);
										return v == null ? "" : v.toString();
									} catch (NoSuchMethodException | SecurityException | IllegalAccessException
											| IllegalArgumentException | InvocationTargetException e) {
									}
								} 
								return t.getName();
							}
						}, (Collection<Resource>) m.invoke(resource));
					}
					return ResourceUtils.implodeResourceValues((Collection<Resource>) m.invoke(resource));
				} else if(String.class.isAssignableFrom(type)) { 
					return ResourceUtils.implodeValues((Collection<String>) m.invoke(resource));
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
		log.error(methodName + " not found", t);
		return "";
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
		
//		if(value==null) {
//			// We don't support setting null values. Caller may have set values on object directly
//			return;
//		}
		
		/* BPS - https://tickets.logonbox.com/app/ui/ticket/M167
		 * 
		 *  We do need to support setting of null values. In the case of I4J
		 *  providers, we must be able to set properties by their key. All of 
		 *  the code below already MOSTLY supported setting null, a couple of 
		 *  tweaks were needed (encrypted strings for example)
		 */
		
		Object resource = resolveTargetEntity(entity, template);
		
		String fieldName = template.hasMapping() ? template.getMapping() : template.getResourceKey();
		while(fieldName.contains(".")) {
			try {
				String entityName = StringUtils.substringBefore(fieldName, ".");
				String entityGetMethod = "get" + StringUtils.capitalize(entityName);
				Method m = resource.getClass().getMethod(entityGetMethod, (Class<?>[])null);
				Class<?> entityClass = m.getReturnType();
				resource = m.invoke(resource, (Object[])null);
				if(resource==null) {
					resource = entityClass.getConstructor((Class<?>[])null).newInstance();
					m = entity.getClass().getMethod("set" + StringUtils.capitalize(entityName), entityClass);
					m.invoke(entity, resource);
				}
				fieldName = StringUtils.substringAfter(fieldName, ".");
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
		}
		
		if(log.isDebugEnabled()) {
			log.debug("Setting {}={} on {}", fieldName, value, resource.getClass().getSimpleName());
		}
		
		Method[] methods = resource.getClass().getMethods();
		
		String methodName = "set" + StringUtils.capitalize(fieldName);

		for(Method m : methods) {
			if(m.getName().equals(methodName)) {
				Class<?> clz = m.getParameterTypes()[0];
				if(clz.isEnum()){
					try {
						Enum<?>[] enumConstants = (Enum<?>[]) resource.getClass().getDeclaredField(template.getResourceKey()).getType().getEnumConstants();
						if(StringUtils.isBlank(value)) {
							m.invoke(resource, (Object)null);
							return;  
						}
						if(NumberUtils.isCreatable(value)){//ordinal
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
						Object object = StringUtils.isBlank(value) ? null : primitiveParsers.get(clz).parseValue(value);
						if(object == null) {
							if(clz.equals(boolean.class)) {
								m.invoke(resource, false);
							}
							else if(clz.equals(int.class)) {
								m.invoke(resource, 0);
							}
							else if(clz.equals(long.class)) {
								m.invoke(resource, 0l);
							}
							else
								m.invoke(resource, object);
						}
						else if(object != null && !object.getClass().equals(clz)) {
							if(clz.equals(boolean.class)) {
								m.invoke(resource, ((Boolean)object).booleanValue());
							}
							else if(clz.equals(int.class)) {
								m.invoke(resource, ((Integer)object).intValue());
							}
							else if(clz.equals(long.class)) {
								m.invoke(resource, ((Long)object).longValue());
							}
							else
								throw new UnsupportedOperationException();
						}
						else
							m.invoke(resource, object);
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
					}  else if(String.class.isAssignableFrom(type)) {
						try {
							Collection<String> values = new ArrayList<String>();
							if(!StringUtils.isEmpty(value)) {
								String[] vals = ResourceUtils.explodeValues(value);
								for(String val : vals) {
									values.add(val);
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
		
		if(Boolean.getBoolean("hypersocket.development")) {
			if(log.isWarnEnabled()) {
				log.warn(template.getResourceKey() + " is not a property of the entity " + resource.getClass().getName());
			}
		}
	}

	@SuppressWarnings({ "unchecked", "deprecation" })
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
			if(value == null)
				return null;
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
		String methodName;
		if(template.hasMapping()) {
			methodName = "get" + StringUtils.capitalize(template.getMapping());
		} else {
			methodName = "get" + StringUtils.capitalize(template.getResourceKey());
		}
		try {

			entity.getClass().getMethod(methodName, (Class<?>[]) null);
			return true;

		} catch (NoSuchMethodException e) {
		} catch (SecurityException e) {
		} catch (IllegalArgumentException e) {
		}
		methodName = "is" + StringUtils.capitalize(template.getResourceKey());
		try {

			entity.getClass().getMethod(methodName, (Class<?>[]) null);
			return true;

		} catch (NoSuchMethodException e) {
		} catch (SecurityException e) {
		} catch (IllegalArgumentException e) {
		}
		return false;
	}
}
