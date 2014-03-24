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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PropertyTemplateRepositoryAbstractImpl implements PropertyTemplateRepository {

	static Logger log = LoggerFactory.getLogger(PropertyTemplateRepositoryAbstractImpl.class);

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

		Element root = doc.getDocumentElement();
		if(root.hasAttribute("extends")) {
			String extendsTemplates = root.getAttribute("extends");
			StringTokenizer t = new StringTokenizer(extendsTemplates, ",");
			while(t.hasMoreTokens()) {
				Enumeration<URL> extendUrls = getClass().getClassLoader().getResources(t.nextToken());
				while(extendUrls.hasMoreElements()) {
					URL extendUrl = extendUrls.nextElement();
					try {
						loadPropertyTemplates(extendUrl);
					} catch(Exception e) {
						log.error("Failed to process " + extendUrl.toExternalForm(), e);
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
					node.getAttribute("resourceBundle"),
					Integer.parseInt(node.getAttribute("weight")));

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
							pnode.getAttribute("resourceKey"),
							generateMetaData(pnode),
							Integer.parseInt(pnode.getAttribute("weight")),
							pnode.hasAttribute("hidden")
									&& pnode.getAttribute("hidden")
											.equalsIgnoreCase("true"),
											Boolean.getBoolean("hypersocket.development") && pnode.hasAttribute("developmentValue") ? pnode.getAttribute("developmentValue") : pnode.getAttribute("defaultValue"));
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
			buf.append("\": \"");
			buf.append(n.getNodeValue());
			buf.append("\"");
		}
		buf.append("}");
		return buf.toString();
	}

	private void loadPropertyStores(Document doc) {

		NodeList list = doc.getElementsByTagName("propertyStore");

		for (int i = 0; i < list.getLength(); i++) {
			Element node = (Element) list.item(i);
			try {
				@SuppressWarnings("unchecked")
				Class<? extends XmlTemplatePropertyStore> clz = (Class<? extends XmlTemplatePropertyStore>) Class
						.forName(node.getAttribute("type"));

				XmlTemplatePropertyStore store = clz.newInstance();
				store.init(node);

				propertyStoresById.put(node.getAttribute("id"), store);
				propertyStores.add(store);
			} catch (Throwable e) {
				log.error("Failed to parse remote extension definition", e);
			}
		}

	}
	
	private void registerPropertyItem(PropertyCategory category,
			PropertyStore propertyStore, String resourceKey, String metaData,
			int weight, boolean hidden, String defaultValue) {

		
		PropertyTemplate template = propertyStore.getPropertyTemplate(resourceKey);
		if (template == null) {
			template = new PropertyTemplate();
			template.setResourceKey(resourceKey);
		}
		
		template.setMetaData(metaData);
		template.setDefaultValue(defaultValue);
		template.setWeight(weight);
		template.setHidden(hidden);
		template.setCategory(category);
		template.setPropertyStore(propertyStore);
		
		propertyStore.registerTemplate(template, resourceXmlPath);
		category.getTemplates().add(template);
		
		Collections.sort(category.getTemplates(), new Comparator<AbstractPropertyTemplate>() {
			@Override
			public int compare(AbstractPropertyTemplate cat1, AbstractPropertyTemplate cat2) {
				return cat1.getWeight().compareTo(cat2.getWeight());
			}
		});
		
		propertyStoresByResourceKey.put(resourceKey, propertyStore);
		
	}

	private PropertyCategory registerPropertyCategory(String resourceKey,
			String bundle, int weight) {

		if (activeCategories.containsKey(resourceKey)) {
			throw new IllegalStateException(
						"Cannot register "
								+ resourceKey
								+ "/"
								+ bundle
								+ " as the resource key is already registered by bundle "
								+ activeCategories.get(resourceKey).getBundle());
		}
		
		PropertyCategory category = new PropertyCategory();
		category.setBundle(bundle);
		category.setCategoryKey(resourceKey);
		category.setWeight(weight);
		
		activeCategories.put(category.getCategoryKey(), category);
		return category;
	}


	@Override
	public String getValue(String resourceKey) {

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

	protected void onValueChanged(PropertyTemplate template, String oldValue, String value) {
		
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
		
		List<PropertyCategory> ret = new ArrayList<PropertyCategory>(activeCategories.values());
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
}
