package com.hypersocket.config;


import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import com.hypersocket.plugins.Plugins;
import com.hypersocket.properties.PropertyCategory;
import com.hypersocket.properties.PropertyStore;
import com.hypersocket.properties.PropertyTemplate;
import com.hypersocket.properties.PropertyTemplateRepository;
import com.hypersocket.properties.PropertyTemplateWeightComparator;
import com.hypersocket.properties.XmlTemplatePropertyStore;

public class PropertyTemplateRepositoryAbstractImpl implements
		PropertyTemplateRepository {

	private final static Logger log = LoggerFactory
			.getLogger(PropertyTemplateRepositoryAbstractImpl.class);

	public static final String SYSTEM_GROUP = "system";

	private Map<String, PropertyStore> propertyStoresByResourceKey = new HashMap<>();
	private Map<String, PropertyStore> propertyStoresById = new HashMap<>();

	private Set<PropertyStore> propertyStores = new HashSet<>();
	private Map<String, PropertyCategory> activeCategories = new HashMap<>();

	private String resourceXmlPath;
	private PropertyStore defaultStore;

	public PropertyTemplateRepositoryAbstractImpl(PropertyStore defaultStore) {
		this.defaultStore = defaultStore;
	}

	@Override
	public final void loadPropertyTemplates(String resourceXmlPath, ClassLoader classLoader) {
		for(var url : Plugins.pluginResources(classLoader, resourceXmlPath)) {
			try {
				loadPropertyTemplates(classLoader, url);
			} catch (Exception e) {
				log.error("Failed to process " + url.toExternalForm(), e);
			}
		}
	}

	@Override
	public final void unloadPropertyTemplates(ClassLoader classLoader) {
		for(var it = activeCategories.values().iterator(); it.hasNext(); ) {
			var cat = it.next();
			if(cat.getClassLoader().equals(classLoader)) {
				it.remove();
			}
			else {
				for(var it2 = cat.getTemplates().iterator(); it2.hasNext() ; ) {
					var pt = it2.next();
					if(pt.getClassLoader().equals(classLoader)) {
						it2.remove();
					}
				}
			}
		}
	}

	private void loadPropertyTemplates(ClassLoader classLoader, URL url) throws SAXException,
			IOException, ParserConfigurationException {

		var xmlFactory = DocumentBuilderFactory.newInstance();
		var xmlBuilder = xmlFactory.newDocumentBuilder();
		var doc = xmlBuilder.parse(url.openStream());

		if (log.isInfoEnabled()) {
			log.info("Loading property template " + url.getPath());
		}

		var root = doc.getDocumentElement();
		if (root.hasAttribute("extends")) {
			var extendsTemplates = root.getAttribute("extends");
			var t = new StringTokenizer(extendsTemplates, ",");
			while (t.hasMoreTokens()) {
				var extendUrls = getClass().getClassLoader()
						.getResources(t.nextToken());
				while (extendUrls.hasMoreElements()) {
					var extendUrl = extendUrls.nextElement();
					try {
						loadPropertyTemplates(classLoader, extendUrl);
					} catch (Exception e) {
						log.error(
								"Failed to process "
										+ extendUrl.toExternalForm(), e);
					}
				}
			}
		}

		loadPropertyStores(classLoader, doc);
		loadPropertyCategories(classLoader, doc);
	}

	private void loadPropertyCategories(ClassLoader classLoader, Document doc) throws IOException {

		var list = doc.getElementsByTagName("propertyCategory");

		for (int i = 0; i < list.getLength(); i++) {
			var node = (Element) list.item(i);

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

			var cat = registerPropertyCategory(classLoader,
					node.getAttribute("resourceKey"),
					node.getAttribute("namespace"),
					node.hasAttribute("group") ? node.getAttribute("group")
							: SYSTEM_GROUP,
					node.getAttribute("resourceBundle"), Integer.parseInt(node
							.getAttribute("weight")),
					node.getAttribute("displayMode"),
					node.hasAttribute("system") && Boolean.parseBoolean(node.getAttribute("system")),
					node.hasAttribute("nonSystem") && Boolean.parseBoolean(node.getAttribute("nonSystem")),
					node.getAttribute("filter"),
					node.hasAttribute("hidden") && Boolean.parseBoolean(node.getAttribute("hidden")),
					node.getAttribute("visibilityDependsOn"),
					node.getAttribute("visibilityDependsValue"),
					node.getAttribute("via"));

			var properties = node.getElementsByTagName("property");

			for (int x = 0; x < properties.getLength(); x++) {

				var pnode = (Element) properties.item(x);

				var store = defaultStore;
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
							classLoader, cat,
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

	private void loadPropertyStores(ClassLoader classLoader, Document doc) {

		var list = doc.getElementsByTagName("propertyStore");

		for (int i = 0; i < list.getLength(); i++) {
			var node = (Element) list.item(i);
			try {

				PropertyStore store = null;

				if(node.hasAttribute("bean")) {
					store = getBean(node.getAttribute("bean"), PropertyStore.class);
				} else {
					@SuppressWarnings("unchecked")
					var clz = (Class<? extends PropertyStore>) Class
							.forName(node.getAttribute("type"));

					store = clz.getConstructor().newInstance();
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

	private void registerPropertyItem(ClassLoader classLoader, PropertyCategory category,
			PropertyStore propertyStore, Element pnode) {

		var resourceKey = pnode.getAttribute("resourceKey");
//		String inputType = pnode.getAttribute("inputType");
		var mapping = pnode.hasAttribute("mapping") ? pnode.getAttribute("mapping") : "";
		var weight = pnode.hasAttribute("weight") ? Integer.parseInt(pnode.getAttribute("weight")) : 9999;
		var hidden = (pnode.hasAttribute("hidden") && pnode.getAttribute("hidden").equalsIgnoreCase("true"))
				|| (pnode.hasAttribute("inputType") && pnode.getAttribute("inputType").equalsIgnoreCase("hidden"));
		var displayMode = pnode.hasAttribute("displayMode") ? pnode.getAttribute("displayMode") : "";
		var readOnly = pnode.hasAttribute("readOnly") && pnode.getAttribute("readOnly").equalsIgnoreCase("true");
		var defaultValue = Boolean.getBoolean("hypersocket.development") && pnode.hasAttribute("developmentValue")
				? pnode.getAttribute("developmentValue") : pnode.hasAttribute("defaultValue") ? pnode.getAttribute("defaultValue") : "";
		var encrypted = pnode.hasAttribute("encrypted") && pnode.getAttribute("encrypted").equalsIgnoreCase("true");
		var defaultsToProperty = pnode.hasAttribute("defaultsToProperty") ? pnode.getAttribute("defaultsToProperty") : null;
		var metaData = generateMetaData(pnode);

		var template = propertyStore.getPropertyTemplate(resourceKey);
		if (template == null) {
			template = new PropertyTemplate();
			template.setResourceKey(resourceKey);
		}

		template.setDefaultValue(defaultValue);
		template.setClassLoader(classLoader);
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
				template.getAttributes().put(n.getNodeName(), n.getNodeValue());
			}
		}

		propertyStore.registerTemplate(template, resourceXmlPath);

		category.getTemplates().remove(template);
		category.getTemplates().add(template);

		Collections.sort(category.getTemplates(),
				new PropertyTemplateWeightComparator());

		propertyStoresByResourceKey.put(resourceKey, propertyStore);

	}

	protected boolean isKnownAttributeName(String name) {

		var getMethod = "get" + StringUtils.capitalize(name);
		try {
			return PropertyTemplate.class.getMethod(getMethod) != null;
		} catch (NoSuchMethodException e) {
			return false;
		}
	}

	private PropertyCategory registerPropertyCategory(ClassLoader classLoader, String categoryKey, String categoryNamespace,
			String categoryGroup, String bundle, int weight, String displayMode, boolean systemOnly, boolean nonSystem, String filter, boolean hidden,
			String visibilityDependsOn, String visibilityDependsValue, String via) {

		if (activeCategories.containsKey(categoryKey)
				&& !activeCategories.get(categoryKey).getBundle().equals(bundle)) {
			throw new IllegalStateException("Cannot register " + categoryKey
					+ "/" + bundle
					+ " as the resource key is already registered by bundle "
					+ activeCategories.get(categoryKey).getBundle());
		}

		if(activeCategories.containsKey(categoryKey)) {
			var existingCategory = activeCategories.get(categoryKey);
			if(existingCategory.isHidden() && !hidden) {
				/* Always show if any are visible */
				log.info(String.format("Multiple registrations of %s, with different visibility. Visible takes precedence", categoryKey));
			}
			else {
				return existingCategory;
			}
		}

		var category = new PropertyCategory();
		category.setBundle(bundle);
		category.setCategoryKey(categoryKey);
		category.setCategoryNamespace(categoryNamespace);
		category.setCategoryGroup(categoryGroup);
		category.setDisplayMode(displayMode);
		category.setWeight(weight);
		category.setSystemOnly(systemOnly);
		category.setNonSystem(nonSystem);
		category.setFilter(filter);
		category.setHidden(hidden);
		category.setVisibilityDependsOn(visibilityDependsOn);
		category.setVisibilityDependsValue(visibilityDependsValue);
		category.setClassLoader(classLoader);

		activeCategories.put(category.getCategoryKey(), category);
		return category;
	}

	@Override
	public String getValue(String resourceKey) {

		if (!propertyStoresByResourceKey.containsKey(resourceKey)) {
			throw new IllegalStateException(
					"No store registered for resource key " + resourceKey);
		}

		var store = propertyStoresByResourceKey.get(resourceKey);

		var template = store.getPropertyTemplate(resourceKey);

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
		var ret = new ArrayList<PropertyCategory>();
		for (var c : activeCategories.values()) {
			if (c.getCategoryGroup().equals(group)) {
				ret.add(c);
			}
		}
		Collections.sort(ret, (cat1, cat2) -> cat1.getWeight().compareTo(cat2.getWeight()));
		return ret;
	}

	@Override
	public String[] getValues(String name) {
		return StringUtils.splitByWholeSeparator(getValue(name), "]|[");
	}

	@Override
	public void setValues(Map<String, String> values) {

		for (var name : values.keySet()) {
			setValue(name, values.get(name));
		}

	}

	@Override
	public PropertyTemplate getPropertyTemplate(String resourceKey) {
		return propertyStoresByResourceKey.get(resourceKey)
				.getPropertyTemplate(resourceKey);
	}
}
