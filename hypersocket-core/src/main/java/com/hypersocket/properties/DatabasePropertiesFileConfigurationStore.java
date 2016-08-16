package com.hypersocket.properties;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.hypersocket.i18n.I18NService;

@Component
public class DatabasePropertiesFileConfigurationStore extends  PropertiesFileConfigurationStore {
	
	public static final String RESOURCE_BUNDLE = "DatabaseService";

	public static final String MYSQL = "MYSQL";
	public static final String POSTGRES = "POSTGRES";

	static Logger log = LoggerFactory.getLogger(DatabasePropertiesFileConfigurationStore.class);
	
	private static String jdbcUrlTemplate = "jdbc:%s://%s:%s/%s";
	
	private static Map<String, String> driverMap = new HashMap<String, String>();
	private static Map<String, String> dialectMap = new HashMap<String, String>();
	
	static {
		driverMap.put(MYSQL, "com.mysql.jdbc.Driver");
		driverMap.put(POSTGRES, "org.postgresql.Driver");
		
		dialectMap.put(MYSQL, "org.hibernate.dialect.MySQL5Dialect");
		dialectMap.put(POSTGRES, "org.hibernate.dialect.PostgreSQLDialect");
	}
	
	@Autowired
	I18NService i18nService;
	
	 @PostConstruct
	 public void init(){
	    	i18nService.registerBundle(RESOURCE_BUNDLE);
	 }
	 
	public DatabasePropertiesFileConfigurationStore() {}
	
	@Override
	public void setProperty(PropertyTemplate template, String value) {
		
		if(Boolean.getBoolean("hypersocket.demo")) {
			throw new IllegalStateException("This is a demo. No changes to resources or settings can be persisted.");
		}
		
		if("jdbc.vendor".equals(template.getResourceKey())){
			String dialect = dialectMap.get(value);
			if(StringUtils.isEmpty(dialect)){
				throw new IllegalArgumentException(String.format("Dialect could not be found for vendor %s", dialect));
			}
			String driver = driverMap.get(value);
			if(StringUtils.isEmpty(driver)){
				throw new IllegalArgumentException(String.format("Driver could not be found for vendor %s", dialect));
			}
			properties.put("jdbc.hibernate.dialect", dialect);
			properties.put("jdbc.driver.className", driver);
			properties.put("jdbc.vendor", value);
			
		}else if("jdbc.username".equals(template.getResourceKey())){
			properties.put("jdbc.username", value);
		}else if("jdbc.password".equals(template.getResourceKey())){
			properties.put("jdbc.password", value);
		}else if("jdbc.name".equals(template.getResourceKey())){
			properties.put("jdbc.database", value);
		}else if("jdbc.host".equals(template.getResourceKey())){
			properties.put("jdbc.host", value);
		}else if("jdbc.port".equals(template.getResourceKey())){
			properties.put("jdbc.port", value);
		}
		
		if(properties.containsKey("jdbc.vendor") && properties.containsKey("jdbc.host") && properties.containsKey("jdbc.port") && properties.containsKey("jdbc.database")){
			String jdbcUrl = String.format(jdbcUrlTemplate, properties.getProperty("jdbc.vendor").toLowerCase(), 
					properties.getProperty("jdbc.host"), properties.getProperty("jdbc.port"), properties.getProperty("jdbc.database"));
			properties.put("jdbc.url", jdbcUrl);
		}
		try {
			saveProperties();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to save property to properties file", e);
		}
	}

	@Override
	public String getPropertyValue(PropertyTemplate template) {
		if(properties.containsKey(template.getResourceKey())) {
			return properties.getProperty(template.getResourceKey());
		} else {
			return template.getDefaultValue();
		}
	}

}
