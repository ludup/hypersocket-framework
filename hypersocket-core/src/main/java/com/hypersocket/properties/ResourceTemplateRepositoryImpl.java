package com.hypersocket.properties;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.annotation.PostConstruct;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.hypersocket.ApplicationContextServiceImpl;
import com.hypersocket.encrypt.EncryptionService;
import com.hypersocket.replace.ReplacementUtils;
import com.hypersocket.resource.SimpleResource;
import com.hypersocket.triggers.ValidationException;

@Repository
public abstract class ResourceTemplateRepositoryImpl extends PropertyRepositoryImpl
		implements ResourceTemplateRepository {

	static Logger log = LoggerFactory.getLogger(ResourceTemplateRepositoryImpl.class);

	private DatabasePropertyStore configPropertyStore;

	@Autowired
	private EncryptionService encryptionService;

	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private Environment environment;
	
	private Map<String, PropertyCategory> activeCategories = new HashMap<>();
	private Map<String, PropertyTemplate> propertyTemplates = new HashMap<>();
	
	private Map<String, PropertyStore> propertyStoresById = new HashMap<>();
	private Set<PropertyStore> propertyStores = new HashSet<>();

	private List<PropertyTemplate> activeTemplates = new ArrayList<>();
	private Set<String> propertyNames = new HashSet<>();
	private Set<String> variableNames = new HashSet<>();

	private Set<PropertyResolver> propertyResolvers = new HashSet<>();
	private String resourceXmlPath;

	static Map<String, List<ResourceTemplateRepository>> propertyContexts = new HashMap<>();
	static Map<String, Set<PropertyTemplate>> propertyTemplatesByType = new HashMap<>();
	
	public ResourceTemplateRepositoryImpl() {
		super();
	}

	@PostConstruct
	private void postConstruct() {
		configPropertyStore = new DatabasePropertyStore(
				(PropertyRepository) applicationContext.getBean("systemConfigurationRepositoryImpl"),
				encryptionService);
		propertyStoresById.put("db", configPropertyStore);
		propertyStoresById.put("transient", new TransientPropertyStore());
	}

	public ResourceTemplateRepositoryImpl(boolean requiresDemoWrite) {
		super(requiresDemoWrite);
	}

	@Override
	public ResourcePropertyStore getDatabasePropertyStore() {
		return configPropertyStore;
	}

	protected ResourcePropertyStore getPropertyStore() {
		return configPropertyStore;
	}

	@Override
	public void registerPropertyResolver(PropertyResolver resolver) {
		propertyResolvers.add(resolver);
	}

	@Override
	public Set<String> getPropertyNames(SimpleResource resource) {

		Set<String> results = new HashSet<>();
		results.addAll(propertyNames);
		for (PropertyResolver r : propertyResolvers) {
			results.addAll(r.getPropertyNames(resource));
		}
		return results;
	}

	@Override
	public Set<String> getPropertyNames(SimpleResource resource, boolean includeResolvers) {

		Set<String> results = new HashSet<>();
		results.addAll(propertyNames);
		if (includeResolvers) {
			for (PropertyResolver r : propertyResolvers) {
				results.addAll(r.getPropertyNames(resource));
			}
		}
		return results;
	}

	@Override
	public Set<String> getVariableNames(SimpleResource resource) {

		Set<String> results = new HashSet<>();
		results.addAll(variableNames);
		for (PropertyResolver r : propertyResolvers) {
			results.addAll(r.getVariableNames(resource));
		}
		return results;
	}

	public static Set<String> getContextNames() {
		return propertyContexts.keySet();
	}
	@Override
	public void loadPropertyTemplates(String resourceXmlPath) {
		loadPropertyTemplates(resourceXmlPath, false);
	}
	
	public void loadPropertyTemplates(String resourceXmlPath, boolean forceReadOnly) {

		this.resourceXmlPath = resourceXmlPath;

		String context = null;
		try {
			Enumeration<URL> urls = getClass().getClassLoader().getResources(resourceXmlPath);
			if (!urls.hasMoreElements()) {
				throw new IllegalArgumentException(resourceXmlPath + " does not exist!");
			}

			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				try {
					context = loadPropertyTemplates(url, forceReadOnly);
				} catch (Exception e) {
					log.error("Failed to process " + url.toExternalForm(), e);
				}
			}

		} catch (IOException e) {
			log.error("Failed to load propertyTemplate.xml resources", e);
		}

		if (context != null) {

			if (log.isInfoEnabled()) {
				log.info("Loading attributes for context " + context);
			}
			if (!propertyContexts.containsKey(context)) {
				propertyContexts.put(context, new ArrayList<ResourceTemplateRepository>());
			}
			propertyContexts.get(context).add(this);
		}

	}

	private void loadPropertyStores(Document doc) {

		NodeList list = doc.getElementsByTagName("propertyStore");

		for (int i = 0; i < list.getLength(); i++) {
			Element node = (Element) list.item(i);
			try {
				@SuppressWarnings("unchecked")
				Class<? extends PropertyStore> clz = (Class<? extends PropertyStore>) Class
						.forName(node.getAttribute("type"));

				PropertyStore store = clz.newInstance();

				if (store instanceof XmlTemplatePropertyStore) {
					((XmlTemplatePropertyStore) store).init(node);
				}

				propertyStoresById.put(node.getAttribute("id"), store);
				propertyStores.add(store);
			} catch (Throwable e) {
				log.error("Failed to parse remote extension definition", e);
			}
		}

	}

	private String loadPropertyTemplates(URL url, boolean forceReadOnly) throws SAXException, IOException, ParserConfigurationException {

		DocumentBuilderFactory xmlFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
		Document doc = xmlBuilder.parse(url.openStream());

		Element root = doc.getDocumentElement();
		String context = null;

		if (root.hasAttribute("extends")) {
			String extendsTemplates = root.getAttribute("extends");
			StringTokenizer t = new StringTokenizer(extendsTemplates, ",");
			while (t.hasMoreTokens()) {
				Enumeration<URL> extendUrls = getClass().getClassLoader().getResources(t.nextToken());
				while (extendUrls.hasMoreElements()) {
					URL extendUrl = extendUrls.nextElement();
					try {
						context = loadPropertyTemplates(extendUrl, forceReadOnly);
					} catch (Exception e) {
						log.error("Failed to process " + extendUrl.toExternalForm(), e);
					}
				}
			}
		} 
		
		if (root.hasAttribute("context")) {
			context = root.getAttribute("context");
		}

		if (log.isInfoEnabled()) {
			log.info("Loading property template resource " + url.toExternalForm());
		}

		loadPropertyStores(doc);

		loadPropertyCategories(doc, forceReadOnly);

		return context;
	}

	private void loadPropertyCategories(Document doc, boolean forceReadOnly) throws IOException {

		NodeList list = doc.getElementsByTagName("propertyCategory");
		
		for (int i = 0; i < list.getLength(); i++) {
			Element node = (Element) list.item(i);

			if (!node.hasAttribute("resourceKey") || !node.hasAttribute("resourceBundle")
					|| !node.hasAttribute("weight")) {
				throw new IOException("<propertyCategory> requires resourceKey, resourceBundle and weight attributes");
			}

			if (log.isDebugEnabled()) {
				log.debug("Registering category " + node.getAttribute("resourceKey") + " for bundle "
						+ node.getAttribute("resourceBundle"));
			}

			String group = "system";
			if (node.hasAttribute("group")) {
				group = node.getAttribute("group");
			}

			PropertyCategory cat = registerPropertyCategory(node.getAttribute("resourceKey"),
					node.getAttribute("namespace"),
					node.getAttribute("resourceBundle"), Integer.parseInt(node.getAttribute("weight")), false, group,
					node.getAttribute("displayMode"), 
					node.hasAttribute("system") && Boolean.parseBoolean(node.getAttribute("system")),
					node.hasAttribute("nonSystem") && Boolean.parseBoolean(node.getAttribute("nonSystem")),
					node.getAttribute("filter"),
					node.hasAttribute("hidden") && Boolean.parseBoolean(node.getAttribute("hidden")),
					node.getAttribute("visibilityDependsOn"),
					node.getAttribute("visibilityDependsValue"),
					node.getAttribute("via"));

			PropertyStore defaultStore = getPropertyStore();

			if (node.hasAttribute("store")) {
				if(!node.getAttribute("store").equals("default")) {
					defaultStore = propertyStoresById.get(node.getAttribute("store"));
					if (defaultStore == null) {
						throw new IOException("PropertyStore " + node.getAttribute("store") + " does not exist!");
					}
				}
			}

			NodeList properties = node.getElementsByTagName("property");

			for (int x = 0; x < properties.getLength(); x++) {

				Element pnode = (Element) properties.item(x);

				if (!pnode.hasAttribute("resourceKey")) {
					throw new IOException("property must have a resourceKey attribute");
				}
				try {

					PropertyTemplate t = propertyTemplates.get(pnode.getAttribute("resourceKey"));
					if (t != null) {
						if (log.isInfoEnabled()) {
							log.info("Overriding default value of " + t.getResourceKey() + " to "
									+ pnode.getAttribute("defaultValue"));
						}
						t.setDefaultValue(pnode.getAttribute("defaultValue"));
						continue;
					}
					
					PropertyStore store = defaultStore;
					if (pnode.hasAttribute("store")) {
						if(pnode.getAttribute("store").equals("default")) {
							store = getPropertyStore();
						} else {
							store = propertyStoresById.get(pnode.getAttribute("store"));
							if (store == null) {
								throw new IOException("PropertyStore " + pnode.getAttribute("store") + " does not exist!");
							}
						}
					}

					registerPropertyItem(cat, store, pnode, forceReadOnly);
				} catch (Throwable e) {
					log.error("Failed to register property item", e);
				}
			}
		}
	}

	private String generateMetaData(Element pnode) {
		NamedNodeMap map = pnode.getAttributes();
		StringBuffer buf = new StringBuffer();
		buf.append("{");
		for (int i = 0; i < map.getLength(); i++) {
			Node n = map.item(i);
			if (buf.length() > 1) {
				buf.append(", ");
			}
			buf.append("\"");
			buf.append(n.getNodeName());
			buf.append("\": ");
			if (n.getNodeName().equals("options")) {
				buf.append("[");
				StringTokenizer t = new StringTokenizer(n.getNodeValue(), ",");
				while (t.hasMoreTokens()) {
					String opt = t.nextToken();
					buf.append("{ \"name\": \"");
					buf.append(opt);
					buf.append("\", \"value\": \"");
					buf.append(opt);
					buf.append("\"}");
					if (t.hasMoreTokens()) {
						buf.append(",");
					}
				}
				buf.append("]");
			} else {
				buf.append("\"");
				buf.append(n.getNodeValue());
				buf.append("\"");
			}
		}
		buf.append("}");
		return buf.toString();
	}

	@Override
	public PropertyTemplate getPropertyTemplate(SimpleResource resource, String resourceKey) {

		for (PropertyResolver r : propertyResolvers) {
			if (r.hasPropertyTemplate(resource, resourceKey)) {
				return r.getPropertyTemplate(resource, resourceKey);
			}
		}

		return propertyTemplates.get(resourceKey);
	}

	private void registerPropertyItem(PropertyCategory category, PropertyStore propertyStore, Element pnode, boolean forceReadOnly) {

	
		String resourceKey = pnode.getAttribute("resourceKey");
		String inputType = pnode.getAttribute("inputType");
		String mapping = pnode.hasAttribute("mapping") ? pnode.getAttribute("mapping") : "";
		int weight = pnode.hasAttribute("weight") ? Integer.parseInt(pnode.getAttribute("weight")) : 9999;
		boolean hidden = (pnode.hasAttribute("hidden") && pnode.getAttribute("hidden").equalsIgnoreCase("true"))
				|| (pnode.hasAttribute("inputType") && pnode.getAttribute("inputType").equalsIgnoreCase("hidden"));
		String displayMode = pnode.hasAttribute("displayMode") ? pnode.getAttribute("displayMode") : "";
		boolean readOnly = forceReadOnly || pnode.hasAttribute("readOnly") && pnode.getAttribute("readOnly").equalsIgnoreCase("true");
		String defaultValue = Boolean.getBoolean("hypersocket.development") && pnode.hasAttribute("developmentValue")
				? pnode.getAttribute("developmentValue") : pnode.hasAttribute("defaultValue") ? pnode.getAttribute("defaultValue") : "";
		boolean isVariable = pnode.hasAttribute("variable") && pnode.getAttribute("variable").equalsIgnoreCase("true");
		boolean encrypted = pnode.hasAttribute("encrypted") && pnode.getAttribute("encrypted").equalsIgnoreCase("true");
		String defaultsToProperty = pnode.hasAttribute("defaultsToProperty") ? pnode.getAttribute("defaultsToProperty") : null;
		String metaData = generateMetaData(pnode);
		
		if (log.isDebugEnabled()) {
			log.debug("Registering property " + resourceKey);
		}
		
		if (defaultValue != null && defaultValue.startsWith("classpath:")) {
			String url = defaultValue.substring(10);
			InputStream in = getClass().getResourceAsStream(url);
			try {
				if (in != null) {
					try {
						defaultValue = IOUtils.toString(in);
					} catch (IOException e) {
						log.error("Failed to load default value classpath resource " + defaultValue, e);
					}
				} else {
					log.error("Failed to load default value classpath resource " + url);
				}
			} finally {
				IOUtils.closeQuietly(in);
			}
		}
		
		if(!hidden) {
			propertyNames.add(resourceKey);	
			if (isVariable) {
				variableNames.add(resourceKey);
			}
		}
		
		PropertyTemplate template = propertyStore.getPropertyTemplate(resourceKey);
		if (template == null) {
			template = new PropertyTemplate();
			template.setResourceKey(resourceKey);
		}

		template.setDefaultValue(defaultValue);
		template.setWeight(weight);
		template.setHidden(hidden);
		template.setDisplayMode(displayMode);
		template.setReadOnly(readOnly);
		template.setMapping(mapping);
		template.setCategory(category);
		template.setEncrypted(encrypted);
		template.setDefaultsToProperty(defaultsToProperty);
		template.setPropertyStore(propertyStore);
		template.setMetaData(metaData);
		
		for(int i = 0; i < pnode.getAttributes().getLength(); i++) {
			Node n = pnode.getAttributes().item(i);
			if(!isKnownAttributeName(n.getNodeName())) {
				template.getAttributes().put(n.getNodeName(), n.getNodeValue());
			}
		}
		
		
		
		if(template.getAttributes().containsKey("profile")) {
			boolean isHA = environment.acceptsProfiles("HA");
			String profile = template.getAttributes().get("profile");
			if(profile.startsWith("!")) {
				profile = profile.substring(1);
				if(profile.equals("HA") && isHA) {
					template.setHidden(true);
				}
			} else {
				if(profile.equals("HA") && !isHA) {
					template.setHidden(true);
				}
			}
		}
		
		propertyStore.registerTemplate(template, resourceXmlPath);
		category.getTemplates().add(template);
		activeTemplates.add(template);
		propertyTemplates.put(resourceKey, template);
		if(!propertyTemplatesByType.containsKey(inputType)) {
			propertyTemplatesByType.put(inputType, new HashSet<PropertyTemplate>());
		}
		propertyTemplatesByType.get(inputType).add(template);
		
		Collections.sort(category.getTemplates(), new Comparator<AbstractPropertyTemplate>() {
			@Override
			public int compare(AbstractPropertyTemplate cat1, AbstractPropertyTemplate cat2) {
				return cat1.getWeight().compareTo(cat2.getWeight());
			}
		});

		Collections.sort(activeTemplates, new Comparator<AbstractPropertyTemplate>() {
			@Override
			public int compare(AbstractPropertyTemplate t1, AbstractPropertyTemplate t2) {
				return t1.getResourceKey().compareTo(t2.getResourceKey());
			}
		});

	}
	
	protected boolean isKnownAttributeName(String name) {
		
		String getMethod = "get" + StringUtils.capitalize(name);
		try {
			return PropertyTemplate.class.getMethod(getMethod) != null;
		} catch (NoSuchMethodException e) {
			return false;
		} 
	}

	private PropertyCategory registerPropertyCategory(String resourceKey, String categoryNamespace, String bundle, int weight,
			boolean userCreated, String group, String displayMode, boolean systemOnly, boolean nonSystem, String filter, boolean hidden,
			String visibilityDependsOn, String visibilityDependsValue, String via) {

		String categoryKey = resourceKey + "/" + bundle;

		if (activeCategories.containsKey(categoryKey)) {
			return activeCategories.get(categoryKey);
		}

		PropertyCategory category = new PropertyCategory();
		category.setBundle(bundle);
		category.setCategoryKey(resourceKey);
		category.setCategoryGroup(group);
		category.setCategoryNamespace(categoryNamespace);
		category.setDisplayMode(displayMode);
		category.setWeight(weight);
		category.setUserCreated(userCreated);
		category.setSystemOnly(systemOnly);
		category.setNonSystem(nonSystem);
		category.setFilter(filter);
		category.setHidden(hidden);
		category.setVisibilityDependsOn(visibilityDependsOn);
		category.setVisibilityDependsValue(visibilityDependsValue);

		activeCategories.put(categoryKey, category);
		return category;
	}

	@Override
	@Transactional(readOnly = true)
	public String getValue(SimpleResource resource, String resourceKey) {
		return getValue(resource, resourceKey, null);
	}
	
	@Override
	@Transactional(readOnly = true)
	public String getValueOrDefault(SimpleResource resource, String resourceKey, String defaultValue) {
		return getValue(resource, resourceKey, defaultValue);
	}

	@Override
	@Transactional(readOnly = true)
	public String getValue(SimpleResource resource, String resourceKey, String defaultValue) {

		PropertyTemplate template = propertyTemplates.get(resourceKey);

		if (template == null) {

			for (PropertyResolver r : propertyResolvers) {
				template = r.getPropertyTemplate(resource, resourceKey);
				if (template != null) {
					break;
				}
			}

			if (template == null) {
				return configPropertyStore.getProperty(resource, resourceKey, defaultValue);
			}
		}
		
		defaultValue = StringUtils.isBlank(defaultValue) ? template.getDefaultValue() : defaultValue;
		if(((ResourcePropertyStore) template.getPropertyStore()).hasPropertyValueSet(template, resource)) {
			String val = ((ResourcePropertyStore) template.getPropertyStore()).getPropertyValue(template, resource);
			return val == null ? defaultValue : val;
		} else{
			return defaultValue;
		}
		
	}

	@Override
	@Transactional(readOnly = true)
	public String getDecryptedValue(SimpleResource resource, String resourceKey) {

		PropertyTemplate template = propertyTemplates.get(resourceKey);

		if (template == null) {

			for (PropertyResolver r : propertyResolvers) {
				template = r.getPropertyTemplate(resource, resourceKey);
				if (template != null) {
					break;
				}
			}

			if (template == null) {
				throw new IllegalStateException(String.format("No property template with name of %s", resourceKey));
			}
		}

		return ((ResourcePropertyStore) template.getPropertyStore()).getDecryptedValue(template, resource);
	}

	@Override
	@Transactional(readOnly = true)
	public Integer getIntValue(SimpleResource resource, String name) throws NumberFormatException {
		try {
			String val = getValue(resource, name);
			if(val==null) {
				// Why are we returning null when we expect a value or the default?
				return Integer.parseInt(getPropertyTemplate(resource, name).getDefaultValue());
			}
			return Integer.parseInt(val);
		} catch (NumberFormatException e) {
			return Integer.parseInt(getPropertyTemplate(resource, name).getDefaultValue());
		}
	}
	
	@Override
	@Transactional(readOnly = true)
	public Integer getIntValueOrDefault(SimpleResource resource, String name, Integer defaultValue) throws NumberFormatException {
		try {
			String val = getValue(resource, name);
			if(val==null) {
				return defaultValue;
			}
			return Integer.parseInt(val);
		} catch (NumberFormatException e) {
			return Integer.parseInt(getPropertyTemplate(resource, name).getDefaultValue());
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Long getLongValue(SimpleResource resource, String name) throws NumberFormatException {
		try {
			return Long.parseLong(getValue(resource, name));
		} catch(NumberFormatException e) { 
			return Long.parseLong(getPropertyTemplate(resource, name).getDefaultValue());
		}
	}

	@Override
	@Transactional(readOnly = true)
	public Date getDateValue(SimpleResource resource, String name) throws NumberFormatException {
		/**
		 * I don't like this but it needs some thorough testing to re-factor to same as 
		 * getIntValue or getDoubleValue
		 */
		try {
			return new Date(getLongValue(resource, name));
		} catch(NumberFormatException e) { 
			
		}
		return null;
	}

	@Override
	@Transactional(readOnly = true)
	public Double getDoubleValue(SimpleResource resource, String resourceKey) {
		try {
			String val = getValue(resource, resourceKey);
			if(val==null) {
				return null;
			}
			return Double.parseDouble(val);
		} catch (NumberFormatException e) {
			return Double.parseDouble(getPropertyTemplate(resource, resourceKey).getDefaultValue());
		}
	}

	@Override
	@Transactional
	public void setDoubleValue(SimpleResource resource, String resourceKey, Double value) {
		setValue(resource, resourceKey, Double.toString(value));
	}

	@Override
	@Transactional
	public void setValue(SimpleResource resource, String name, Long value) {
		setValue(resource, name, Long.toString(value));
	}

	@Override
	@Transactional
	public void setValue(SimpleResource resource, String name, Date value) {
		setValue(resource, name, value.getTime());
	}

	@Override
	@Transactional(readOnly = true)
	public Boolean getBooleanValue(SimpleResource resource, String name) {
		return Boolean.parseBoolean(getValue(resource, name));
	}
	
	@Override
	@Transactional(readOnly = true)
	public Boolean getBooleanValueOrDefault(SimpleResource resource, String name, Boolean defaultValue) {
		try {
			String val = getValue(resource, name);
			if(val==null) {
				return defaultValue;
			}
			return Boolean.parseBoolean(val);
		} catch (NumberFormatException e) {
			return Boolean.parseBoolean(getPropertyTemplate(resource, name).getDefaultValue());
		}
		
	}

	
	@Override
	public boolean hasPropertyTemplate(SimpleResource resource, String key) {
		return propertyTemplates.containsKey(key);
	}

	@Override
	public boolean hasPropertyValueSet(SimpleResource resource, String resourceKey) {
		PropertyTemplate template = getPropertyTemplate(resource, resourceKey);
		return template != null && ((ResourcePropertyStore) template.getPropertyStore()).hasPropertyValueSet(template, resource);
	}

	@Override
	@Transactional
	public void setValues(SimpleResource resource, Map<String, String> properties) {

		if (properties != null) {
			for (String resourceKey : properties.keySet()) {
				
				/*
				 * I'm sure this setValue() / setValues() in
				 * ResourceTemplateRepositoryImpl is wrong (i.e. this reversion
				 * is still wrong). i dont understand why setValues() behaves
				 * differently to setValue(). If you go through setValue(), it
				 * will correctly store the attribute in the database (as
				 * opposed to tryying to set it via reflect on the entity),
				 * because it finds the PropertyTemplate either from the
				 * propertyTemplates map OR by going through the resolvers
				 * (which is what i want). But in in setValues(), it will only
				 * ever work if its in the map. that doesnt make sense. surely
				 * it should be either one thing or the other, or at least
				 * setValue() and setValues() should have very different names.
				 * 
				 * Is there problem here actually setValue() falling through
				 * to configPropertyStore.setValue() if the template cannot be
				 * found any other way?
				 */
				
//				if (propertyTemplates.containsKey(resourceKey)) {
					setValue(resource, resourceKey, properties.get(resourceKey));
//				}
//				else {
//					log.warn(String.format("Request to set property %s which does not exist in template %s", resourceKey, resource.toString()));
//				}
			}
		}
	}

	@Override
	@Transactional
	public void setValue(SimpleResource resource, String resourceKey, String value) {

		PropertyTemplate template = propertyTemplates.get(resourceKey);

		if (template == null) {
			if(log.isDebugEnabled()) {
				log.debug("There is no template for {} in this repository", resourceKey);
			}
			for (PropertyResolver r : propertyResolvers) {
				template = r.getPropertyTemplate(resource, resourceKey);
				if (template != null) {
					if(log.isDebugEnabled()) {
						log.debug("Found template for {} in {}", resourceKey, r.getClass().getSimpleName());
					}
					break;
				}
				if(log.isDebugEnabled()) {
					log.debug("There is no template for {} in {}", resourceKey, r.getClass().getSimpleName());
				}
			}

			if (template == null) {
				if(log.isDebugEnabled()) {
					log.debug("No template could be resolved for {}. Sending to database store.", resourceKey);
				}
				configPropertyStore.setProperty(resource, resourceKey, value);
				return;
			}
		}

		 if (template.isReadOnly()) {
			 if(log.isDebugEnabled()) {
					log.debug("The template for {} is read only", resourceKey);
				}
			 return;
		 }
		 
		 validate(template, value);

		 if(log.isDebugEnabled()) {
			log.debug("Passing {} to {}", resourceKey, template.getPropertyStore().getClass().getSimpleName());
		 }
		 
		 ((ResourcePropertyStore) template.getPropertyStore()).setPropertyValue(template, resource, value);
	}

	protected void clearPropertyCache(SimpleResource resource) {
		getPropertyStore().clearPropertyCache(resource);
	}
	
	private void validate(PropertyTemplate template, String value) {
		

		/**
		 * LDP - this needs extending. Its design to support non-ui based
		 * submissions and provide last defense against bad client submitting
		 * invalid values
		 */
	
		if(StringUtils.isBlank(value)) {
			/**
			 * Will use default value;
			 */
			return;
		}
		
		if(ReplacementUtils.isVariable(value) 
				&& template.getAttributes().containsKey("allowAttribute") 
				&& Boolean.parseBoolean(template.getAttributes().get("allowAttribute"))) {
			return;
		}
		
		if(template.getAttributes().containsKey("inputType")) {
			
			String inputType = template.getAttributes().get("inputType");
			switch(inputType) {
			case "integer":
				try {
					Integer.parseInt(value);
				} catch (NumberFormatException e) {
					throw new IllegalArgumentException(String.format("Invalid integer value %s", value));
				}
				break;
			case "slider":
			case "long":
				try {
					Long.parseLong(value);
				} catch (NumberFormatException e) {
					if(template.getAttributes().containsKey("allowAttribute") && Boolean.parseBoolean(template.getAttributes().get("allowAttribute"))) {
						break;
					}
					throw new IllegalArgumentException(String.format("Invalid long value %s", value));
				}
				break;
			default:
				break;
			}
		}
		
		if(template.getAttributes().containsKey("validationBean")) {
			try {
				PropertyValidator v = ApplicationContextServiceImpl.getInstance().getBean(
						template.getAttributes().get("validationBean"), PropertyValidator.class);
				v.validate(template, value);
			} catch (ValidationException e) {
				throw new IllegalStateException(e.getMessage(), e);
			}
			
		}
	}

	@Override
	@Transactional
	public void setValue(SimpleResource resource, String resourceKey, Integer value) {
		setValue(resource, resourceKey, String.valueOf(value));
	}

	@Override
	@Transactional
	public void setValue(SimpleResource resource, String name, Boolean value) {
		setValue(resource, name, String.valueOf(value));
	}

	@Override
	public Collection<PropertyCategory> getPropertyCategories(SimpleResource resource, PropertyFilter... filters) {

		Map<String,PropertyCategory> cats = new HashMap<>();

		for (PropertyCategory c : activeCategories.values()) {

			PropertyCategory tmp;
			
			if(!cats.containsKey(c.getCategoryKey())) {
				tmp = new PropertyCategory(c);
			} else {
				tmp = cats.get(c.getCategoryKey());
			}

			filter(resource, c, tmp, filters);
			if(!tmp.getTemplates().isEmpty())
				cats.put(c.getCategoryKey(), tmp);
		}

		for (PropertyResolver r : propertyResolvers) {
			for (PropertyCategory c : r.getPropertyCategories(resource)) {
				PropertyCategory tmp;
				
				if(!cats.containsKey(c.getCategoryKey())) {
					tmp = new PropertyCategory(c);
				} else {
					tmp = cats.get(c.getCategoryKey());
				}
				filter(resource, c, tmp, filters);
				if(!tmp.getTemplates().isEmpty())
					cats.put(c.getCategoryKey(), tmp);
			}
		}

		List<PropertyCategory> list = new ArrayList<>(cats.values());
		Collections.sort(list, new Comparator<PropertyCategory>() {
			@Override
			public int compare(PropertyCategory cat1, PropertyCategory cat2) {
				return cat1.getWeight().compareTo(cat2.getWeight());
			}
		});

		return list;
	}

	protected void filter(SimpleResource resource, PropertyCategory category, PropertyCategory filteredCategory, PropertyFilter... filters) {
		for (AbstractPropertyTemplate t : category.getTemplates()) {
			if(filters.length == 0)
				filteredCategory.getTemplates().add(new ResourcePropertyTemplate(t, resource));
			else {
				boolean add = true;
				var newTemplate = new ResourcePropertyTemplate(t, resource);
				for(PropertyFilter filter : filters) {
					if(!filter.filterProperty(newTemplate)) {
						add = false;
						break;
					}
				}
				if(add) {
					filteredCategory.getTemplates().add(newTemplate);
				}
			}
		}
	}

	@Override
	public Collection<PropertyCategory> getPropertyCategories(SimpleResource resource, String group) {

		Map<String,PropertyCategory> cats = new HashMap<>();
		for (PropertyCategory c : activeCategories.values()) {
			if (!c.getCategoryGroup().equals(group)) {
				continue;
			}

			PropertyCategory tmp;
			
			if(!cats.containsKey(c.getCategoryKey())) {
				tmp = new PropertyCategory(c);

			} else {
				tmp = cats.get(c.getCategoryKey());
			}
			
			filter(resource, c, tmp);
			cats.put(c.getCategoryKey(), tmp);
		}

		for (PropertyResolver r : propertyResolvers) {
			for (PropertyCategory c : r.getPropertyCategories(resource)) {
				if (!c.getCategoryGroup().equals(group)) {
					continue;
				}
				PropertyCategory tmp;
				
				if(!cats.containsKey(c.getCategoryKey())) {
					tmp = new PropertyCategory(c);

				} else {
					tmp = cats.get(c.getCategoryKey());
				}

				filter(resource, c, tmp);
				cats.put(c.getCategoryKey(), tmp);
			}
		}
		
		List<PropertyCategory> list = new ArrayList<>(cats.values());
		Collections.sort(list, new Comparator<PropertyCategory>() {
			@Override
			public int compare(PropertyCategory cat1, PropertyCategory cat2) {
				return cat1.getWeight().compareTo(cat2.getWeight());
			}
		});

		return list;
	}

	@Override
	public Collection<PropertyTemplate> getPropertyTemplates(SimpleResource resource) {

		Set<PropertyTemplate> results = new HashSet<>();
		results.addAll(activeTemplates);
		for (PropertyResolver r : propertyResolvers) {
			results.addAll(r.getPropertyTemplates(resource));
		}
		return Collections.unmodifiableCollection(results);
	}

	@Override
	public Map<String, PropertyTemplate> getRepositoryTemplates() {
		Map<String, PropertyTemplate> result = new HashMap<>();
		for (PropertyTemplate t : activeTemplates) {
			result.put(t.getResourceKey(), t);
		}
		return result;
	}

	@Override
	@Transactional(readOnly = true)
	public String[] getValues(SimpleResource resource, String name) {

		String values = getValue(resource, name);
		return ResourceUtils.explodeValues(values);
	}

	@Override
	@Transactional(readOnly=true)
	public Long[] getLongValues(SimpleResource resource, String name) {
		String[] values = getValues(resource, name);
		Long[] results = new Long[values.length];
		for(int i=0;i<values.length;i++) {
			results[i] = Long.parseLong(values[i]);
		}
		return results;
	}
	
	@Override
	@Transactional(readOnly=true)
	public Integer[] getIntValues(SimpleResource resource, String name) {
		String[] values = getValues(resource, name);
		Integer[] results = new Integer[values.length];
		for(int i=0;i<values.length;i++) {
			results[i] = Integer.parseInt(values[i]);
		}
		return results;
	}
	
	@Override
	@Transactional(readOnly = true)
	public Map<String, String> getProperties(SimpleResource resource) {

		Map<String, String> properties = new HashMap<>();
		for (PropertyTemplate template : getPropertyTemplates(resource)) {
			properties.put(template.getResourceKey(), getValue(resource, template.getResourceKey()));
		}
		return properties;
	}
	
	@Override
	@Transactional(readOnly = true)
	public Map<String, String> getProperties(SimpleResource resource, boolean decrypt) {

		Map<String, String> properties = new HashMap<>();
		for (PropertyTemplate template : getPropertyTemplates(resource)) {
			if(template.isEncrypted()) {
				properties.put(template.getResourceKey(), getDecryptedValue(resource, template.getResourceKey()));
			} else {
				properties.put(template.getResourceKey(), getValue(resource, template.getResourceKey()));
			}
		}
		return properties;
	}

	public static Map<String, List<ResourceTemplateRepository>> getPropertyContexts() {
		return Collections.unmodifiableMap(propertyContexts);
	}
}
