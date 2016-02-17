package com.hypersocket.properties;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PropertyTemplateRepositoryAbstractImpl implements
		PropertyTemplateRepository {

	static Logger log = LoggerFactory
			.getLogger(PropertyTemplateRepositoryAbstractImpl.class);

	public static final String SYSTEM_GROUP = "system";

	Map<String, PropertyStore> propertyStoresByResourceKey = new HashMap<String, PropertyStore>();
	Map<String, PropertyStore> propertyStoresById = new HashMap<String, PropertyStore>();

	Set<PropertyStore> propertyStores = new HashSet<PropertyStore>();
	Map<String, PropertyCategory> activeCategories = new HashMap<String, PropertyCategory>();

	String resourceXmlPath;

	PropertyStore defaultStore;

	public PropertyTemplateRepositoryAbstractImpl(PropertyStore defaultStore) {
		this.defaultStore = defaultStore;
	}

	public void loadPropertyTemplates(String resourceXmlPath) {

		try {
			Enumeration<URL> urls = getClass().getClassLoader().getResources(
					resourceXmlPath);
			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				try {
					loadPropertyTemplates(url);
				} catch (Exception e) {
					log.error("Failed to process " + url.toExternalForm(), e);
				}
			}
		} catch (IOException e) {
			log.error("Failed to load propertyTemplate.xml resources", e);
		}
	}

	private void loadPropertyTemplates(URL url) throws SAXException,
			IOException, ParserConfigurationException {

		DocumentBuilderFactory xmlFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();
		Document doc = xmlBuilder.parse(url.openStream());

		if (log.isInfoEnabled()) {
			log.info("Loading property template " + url.getPath());
		}

		Element root = doc.getDocumentElement();
		if (root.hasAttribute("extends")) {
			String extendsTemplates = root.getAttribute("extends");
			StringTokenizer t = new StringTokenizer(extendsTemplates, ",");
			while (t.hasMoreTokens()) {
				Enumeration<URL> extendUrls = getClass().getClassLoader()
						.getResources(t.nextToken());
				while (extendUrls.hasMoreElements()) {
					URL extendUrl = extendUrls.nextElement();
					try {
						loadPropertyTemplates(extendUrl);
					} catch (Exception e) {
						log.error(
								"Failed to process "
										+ extendUrl.toExternalForm(), e);
					}
				}
			}
		}

		loadPropertyStores(doc);

		loadPropertyCategories(doc);
	}

	private void loadPropertyCategories(Document doc) throws IOException {

		NodeList list = doc.getElementsByTagName("propertyCategory");

		for (int i = 0; i < list.getLength(); i++) {
			Element node = (Element) list.item(i);

			if (!node.hasAttribute("resourceKey")
					|| !node.hasAttribute("resourceBundle")
					|| !node.hasAttribute("weight")) {
				throw new IOException(
						"<propertyCategory> requires resourceKey, resourceBundle and weight attributes");
			}

			if (log.isInfoEnabled()) {
				log.info("Registering property category with resourceKey "
						+ node.getAttribute("resourceKey"));
			}

			PropertyCategory cat = registerPropertyCategory(
					node.getAttribute("resourceKey"),
					node.hasAttribute("group") ? node.getAttribute("group")
							: SYSTEM_GROUP,
					node.getAttribute("resourceBundle"), Integer.parseInt(node
							.getAttribute("weight")),
					node.getAttribute("displayMode"),
					node.hasAttribute("system") && Boolean.parseBoolean(node.getAttribute("system")),
					node.getAttribute("filter"),
					node.hasAttribute("hidden") && Boolean.parseBoolean(node.getAttribute("hidden")));

			NodeList properties = node.getElementsByTagName("property");

			for (int x = 0; x < properties.getLength(); x++) {

				Element pnode = (Element) properties.item(x);

				PropertyStore store = defaultStore;
				if (pnode.hasAttribute("store")) {
					store = propertyStoresById.get(pnode.getAttribute("store"));
					if (store == null) {
						throw new IOException("PropertyStore "
								+ pnode.getAttribute("store")
								+ " does not exist!");
					}
				}

				try {
					registerPropertyItem(
							cat,
							store,
							pnode);
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
	
	protected <T> T getBean(String name, Class<T> clz) {
		return null;
	}

	private void loadPropertyStores(Document doc) {

		NodeList list = doc.getElementsByTagName("propertyStore");

		for (int i = 0; i < list.getLength(); i++) {
			Element node = (Element) list.item(i);
			try {
				
				PropertyStore store = null;
				
				if(node.hasAttribute("bean")) {
					store = getBean(node.getAttribute("bean"), PropertyStore.class);
				} else {
					@SuppressWarnings("unchecked")
					Class<? extends PropertyStore> clz = (Class<? extends PropertyStore>) Class
							.forName(node.getAttribute("type"));
	
					store = clz.newInstance();
				}
				if(store instanceof XmlTemplatePropertyStore) {
					((XmlTemplatePropertyStore)store).init(node);
				}

				propertyStoresById.put(node.getAttribute("id"), store);
				propertyStores.add(store);
			} catch (Throwable e) {
				log.error("Failed to parse remote extension definition", e);
			}
		}

	}

	private void registerPropertyItem(PropertyCategory category,
			PropertyStore propertyStore, Element pnode) {

		String resourceKey = pnode.getAttribute("resourceKey");
//		String inputType = pnode.getAttribute("inputType");
		String mapping = pnode.hasAttribute("mapping") ? pnode.getAttribute("mapping") : "";
		int weight = pnode.hasAttribute("weight") ? Integer.parseInt(pnode.getAttribute("weight")) : 9999;
		boolean hidden = pnode.hasAttribute("hidden") && pnode.getAttribute("hidden").equalsIgnoreCase("true");
		String displayMode = pnode.hasAttribute("displayMode") ? pnode.getAttribute("displayMode") : "";
		boolean readOnly = pnode.hasAttribute("readOnly") && pnode.getAttribute("readOnly").equalsIgnoreCase("true");
		String defaultValue = Boolean.getBoolean("hypersocket.development") && pnode.hasAttribute("developmentValue")
				? pnode.getAttribute("developmentValue") : pnode.hasAttribute("defaultValue") ? pnode.getAttribute("defaultValue") : "";
		boolean encrypted = pnode.hasAttribute("encrypted") && pnode.getAttribute("encrypted").equalsIgnoreCase("true");
		String defaultsToProperty = pnode.hasAttribute("defaultsToProperty") ? pnode.getAttribute("defaultsToProperty") : null;
		String metaData = generateMetaData(pnode);
		
		PropertyTemplate template = propertyStore
				.getPropertyTemplate(resourceKey);
		if (template == null) {
			template = new PropertyTemplate();
			template.setResourceKey(resourceKey);
		}

		template.setDefaultValue(defaultValue);
		template.setWeight(weight);
		template.setHidden(hidden);
		template.setDisplayMode(displayMode);
		template.setReadOnly(readOnly);
		template.setCategory(category);
		template.setEncrypted(encrypted);
		template.setDefaultsToProperty(defaultsToProperty);
		template.setMapping(mapping);
		template.setPropertyStore(propertyStore);
		template.setMetaData(metaData);

		for(int i = 0; i < pnode.getAttributes().getLength(); i++) {
			Node n = pnode.getAttributes().item(i);
			if(!isKnownAttributeName(n.getNodeName())) {
				template.attributes.put(n.getNodeName(), n.getNodeValue());
			}
		}
		
		propertyStore.registerTemplate(template, resourceXmlPath);
		
		category.getTemplates().remove(template);
		category.getTemplates().add(template);

		Collections.sort(category.getTemplates(),
				new Comparator<AbstractPropertyTemplate>() {
					@Override
					public int compare(AbstractPropertyTemplate cat1,
							AbstractPropertyTemplate cat2) {
						return cat1.getWeight().compareTo(cat2.getWeight());
					}
				});

		propertyStoresByResourceKey.put(resourceKey, propertyStore);

	}

	protected boolean isKnownAttributeName(String name) {
		
		String getMethod = "get" + StringUtils.capitalize(name); 
		try {
			return PropertyTemplate.class.getMethod(getMethod) != null;
		} catch (NoSuchMethodException e) {
			return false;
		} 
	}

	private PropertyCategory registerPropertyCategory(String categoryKey,
			String categoryGroup, String bundle, int weight, String displayMode, boolean systemOnly, String filter, boolean hidden) {

		if (activeCategories.containsKey(categoryKey) 
				&& !activeCategories.get(categoryKey).getBundle().equals(bundle)) {
			throw new IllegalStateException("Cannot register " + categoryKey
					+ "/" + bundle
					+ " as the resource key is already registered by bundle "
					+ activeCategories.get(categoryKey).getBundle());
		}
		
		if(activeCategories.containsKey(categoryKey)) {
			return activeCategories.get(categoryKey);
		}

		PropertyCategory category = new PropertyCategory();
		category.setBundle(bundle);
		category.setCategoryKey(categoryKey);
		category.setCategoryGroup(categoryGroup);
		category.setDisplayMode(displayMode);
		category.setWeight(weight);
		category.setSystemOnly(systemOnly);
		category.setFilter(filter);
		category.setHidden(hidden);
		
		activeCategories.put(category.getCategoryKey(), category);
		return category;
	}

	@Override
	public String getValue(String resourceKey) {

		if (!propertyStoresByResourceKey.containsKey(resourceKey)) {
			throw new IllegalStateException(
					"No store registerd for resource key " + resourceKey);
		}

		PropertyStore store = propertyStoresByResourceKey.get(resourceKey);

		PropertyTemplate template = store.getPropertyTemplate(resourceKey);

		if (template == null) {
			throw new IllegalStateException(resourceKey
					+ " is not a registered configuration item");
		}

		return store.getPropertyValue(template);
	}

	@Override
	public Integer getIntValue(String name) throws NumberFormatException {
		return Integer.parseInt(getValue(name));
	}

	@Override
	public Boolean getBooleanValue(String name) {
		return Boolean.parseBoolean(getValue(name));
	}

	@Override
	public void setValue(String resourceKey, String value) {

		PropertyStore store = propertyStoresByResourceKey.get(resourceKey);

		PropertyTemplate template = store.getPropertyTemplate(resourceKey);

		if (template == null) {
			throw new IllegalStateException(resourceKey
					+ " is not a registered configuration item");
		}

		String oldValue = store.getPropertyValue(template);

		store.setProperty(template, value);

		if (log.isInfoEnabled()) {
			log.info("Changed configuration item " + resourceKey + " from "
					+ oldValue + " to " + value);
		}

		onValueChanged(template, oldValue, value);

	}

	protected void onValueChanged(PropertyTemplate template, String oldValue,
			String value) {

	}

	@Override
	public void setValue(String resourceKey, Integer value) {
		setValue(resourceKey, String.valueOf(value));
	}

	@Override
	public void setValue(String name, Boolean value) {
		setValue(name, String.valueOf(value));
	}

	@Override
	public Collection<PropertyCategory> getPropertyCategories() {
		return getPropertyCategories(SYSTEM_GROUP);
	}

	@Override
	public Collection<PropertyCategory> getPropertyCategories(String group) {

		List<PropertyCategory> ret = new ArrayList<PropertyCategory>();
		for (PropertyCategory c : activeCategories.values()) {
			if (c.getCategoryGroup().equals(group)) {
				ret.add(c);
			}
		}
		Collections.sort(ret, new Comparator<PropertyCategory>() {
			@Override
			public int compare(PropertyCategory cat1, PropertyCategory cat2) {
				return cat1.getWeight().compareTo(cat2.getWeight());
			}
		});
		return ret;
	}

	@Override
	public String[] getValues(String name) {

		String values = getValue(name);
		StringTokenizer t = new StringTokenizer(values, "]|[");
		List<String> ret = new ArrayList<String>();
		while (t.hasMoreTokens()) {
			ret.add(t.nextToken());
		}
		return ret.toArray(new String[0]);
	}

	@Override
	public void setValues(Map<String, String> values) {

		for (String name : values.keySet()) {
			setValue(name, values.get(name));
		}

	}

	@Override
	public PropertyTemplate getPropertyTemplate(String resourceKey) {
		return propertyStoresByResourceKey.get(resourceKey)
				.getPropertyTemplate(resourceKey);
	}
}
