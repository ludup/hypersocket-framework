package com.hypersocket.properties;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

public class DatabasePropertiesFileConfigurationStore extends  PropertiesFileConfigurationStore {

	public static final String MYSQL = "MYSQL";
	public static final String POSTGRES = "POSTGRES";
	public static final String MSSQL = "MSSQL";
	public static final String DERBY = "DERBY";
	public static final String H2 = "H2";

	public static final String JDBC_HIBERNATE_DIALECT = "jdbc.hibernate.dialect";
	public static final String JDBC_DRIVER_CLASS_NAME = "jdbc.driver.className";
	public static final String JDBC_VENDOR = "jdbc.vendor";
	public static final String JDBC_USERNAME = "jdbc.username";
	public static final String JDBC_PASSWORD = "jdbc.password";
	public static final String JDBC_DATABASE = "jdbc.database";
	public static final String JDBC_HOST = "jdbc.host";
	public static final String JDBC_PORT = "jdbc.port";
	public static final String JDBC_URL = "jdbc.url";
	public static final String JDBC_MAX_POOL_SIZE = "jdbc.maxPoolSize";
	public static final String JDBC_MAX_STATEMENTS_PER_CONNECTION = "jdbc.maxStatementsPerConnection"; // 500
	public static final String JDBC_NUM_HELPER_THREADS = "jdbc.numHelperThreads"; // 20
	public static final String JDBC_ACQUIRE_RETRY_DELAY = "jdbc.acquireRetryDelay"; // 1000
	public static final String JDBC_ACQUIRE_RETRY_ATTEMPTS = "jdbc.acquireRetryAttempts"; // 10
	public static final String JDBC_TIMEZONE = "jdbc.timeZone"; // 10
	

	public static final String INSTALLER_DATABASE_HOST = "${installer:databaseHost}";
	public static final String INSTALLER_DATABASE_PORT = "${installer:databasePort}";
	public static final String INSTALLER_DATABASE_NAME = "${installer:databaseName}";
	public static final String INSTALLER_DATABASE_TIMEZONE = "${installer:databaseTimezone}";
	public static final String DERBY_DATA = "derby:data";

	static Logger log = LoggerFactory.getLogger(DatabasePropertiesFileConfigurationStore.class);
	
	private Map<String, Properties> propertiesMap = new HashMap<>();

	@Override
	public void init(Element element) throws IOException {
		super.init(element);
		fillUpMissingKeys();
	}
	
	private Properties getProperties(String vendor) {
	 	try {
			if (!propertiesMap.containsKey(vendor)) {
				InputStream inputStream = DatabasePropertiesFileConfigurationStore.class.getClassLoader().
				getResourceAsStream(String.format("dbtemplates/%s_database.properties", vendor.toLowerCase()));
				propertiesMap.put(vendor, readProperties(inputStream));
			}

			return propertiesMap.get(vendor);
		}catch (IOException e){
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	 
	@Override
	public void setProperty(PropertyTemplate template, String value) {
		try {
			if(Boolean.getBoolean("hypersocket.demo")) {
				throw new IllegalStateException("This is a demo. No changes to resources or settings can be persisted.");
			}

			String currentVendor = getCurrentVendor(template, value);

			Properties vendorTemplateProperties = getProperties(currentVendor);


			if(JDBC_VENDOR.equals(template.getResourceKey())){
				String dialect = vendorTemplateProperties.getProperty(JDBC_HIBERNATE_DIALECT);
				if(StringUtils.isEmpty(dialect)){
					throw new IllegalArgumentException(String.format("Dialect could not be found for vendor %s", dialect));
				}
				String driver = vendorTemplateProperties.getProperty(JDBC_DRIVER_CLASS_NAME);
				if(StringUtils.isEmpty(driver)){
					throw new IllegalArgumentException(String.format("Driver could not be found for vendor %s", dialect));
				}
				properties.put(JDBC_HIBERNATE_DIALECT, dialect);
				properties.put(JDBC_DRIVER_CLASS_NAME, driver);
				properties.put(JDBC_VENDOR, value);

			}else if(JDBC_USERNAME.equals(template.getResourceKey())){
				properties.put(JDBC_USERNAME, value);
			}else if(JDBC_PASSWORD.equals(template.getResourceKey())){
				properties.put(JDBC_PASSWORD, value);
			}else if(JDBC_DATABASE.equals(template.getResourceKey())){
				properties.put(JDBC_DATABASE, value);
			}else if(JDBC_HOST.equals(template.getResourceKey())){
				if(DERBY.equals(currentVendor) || H2.equals(currentVendor)) {
					properties.put(JDBC_HOST, null);
				}else{
					properties.put(JDBC_HOST, value);
				}
			}else if(JDBC_TIMEZONE.equals(template.getResourceKey())){
				if(MYSQL.equals(currentVendor)) {
					properties.put(JDBC_TIMEZONE, value);
				}else{
					properties.put(JDBC_TIMEZONE, null);
				}
			}else if(JDBC_PORT.equals(template.getResourceKey())){
				if(DERBY.equals(currentVendor) || H2.equals(currentVendor)) {
					properties.put(JDBC_PORT, null);
				}else{
					properties.put(JDBC_PORT, value);
				}
			}
			else if(JDBC_MAX_POOL_SIZE.equals(template.getResourceKey()) || 
					JDBC_MAX_STATEMENTS_PER_CONNECTION.equals(template.getResourceKey()) ||
					JDBC_ACQUIRE_RETRY_ATTEMPTS.equals(template.getResourceKey()) ||
					JDBC_ACQUIRE_RETRY_DELAY.equals(template.getResourceKey())) {

				properties.put(template.getResourceKey(), value);
			}

			if(properties.containsKey(JDBC_VENDOR) && properties.containsKey(JDBC_HOST) && properties.containsKey(JDBC_PORT) && properties.containsKey(JDBC_DATABASE)){
				String jdbcUrlTemplate = vendorTemplateProperties.getProperty(JDBC_URL);
				String jdbcUrl = StringUtils.replaceEach(jdbcUrlTemplate,
										new String[] {INSTALLER_DATABASE_HOST, INSTALLER_DATABASE_PORT,
												INSTALLER_DATABASE_NAME, DERBY_DATA,
												INSTALLER_DATABASE_TIMEZONE},
										new String[] {properties.getProperty(JDBC_HOST), properties.getProperty(JDBC_PORT),
												properties.getProperty(JDBC_DATABASE),
												String.format("derby:%s", properties.getProperty(JDBC_DATABASE)),
												URLEncoder.encode(properties.getProperty(JDBC_TIMEZONE), "UTF-8")});

				properties.put(JDBC_URL, jdbcUrl);
			}

			saveProperties();
		} catch (IOException e) {
			throw new IllegalStateException("Failed to save property to properties file", e);
		}
	}

	@Override
	public String getPropertyValue(PropertyTemplate template) {
		String key = template.getResourceKey();
		if(properties.containsKey(key)) {
			return properties.getProperty(key);
		} else {
			return template.getDefaultValue();
		}
	}


	private void fillUpMissingKeys(){
		String jdbcUrl = properties.getProperty(JDBC_URL);
		if(StringUtils.isNotBlank(jdbcUrl)) {
			Map<String, String> tokens = JDBCURIParser.parse(jdbcUrl);
			properties.setProperty(JDBC_VENDOR, tokens.get(JDBCURIParser.SCHEME));
			if(tokens.containsKey(JDBCURIParser.HOST))
				properties.setProperty(JDBC_HOST, tokens.get(JDBCURIParser.HOST));
			if(tokens.containsKey(JDBCURIParser.PORT))
				properties.setProperty(JDBC_PORT, tokens.get(JDBCURIParser.PORT));
			if(tokens.containsKey(JDBCURIParser.DATABASE))
				properties.setProperty(JDBC_DATABASE, tokens.get(JDBCURIParser.DATABASE));
			if(tokens.containsKey(JDBCURIParser.MYSQL_TIMEZONE)) {
				properties.setProperty(JDBC_TIMEZONE, tokens.get(JDBCURIParser.MYSQL_TIMEZONE));
			}
		}
	}

	private String getCurrentVendor(PropertyTemplate template, String value) {
		String currentVendor;
		if(JDBC_VENDOR.equals(template.getResourceKey())){
			currentVendor = value;
		}else {
			PropertyTemplate propertyTemplate = new PropertyTemplate();
			propertyTemplate.setResourceKey(JDBC_VENDOR);
			currentVendor = getPropertyValue(propertyTemplate);
		}
		return currentVendor;
	}

	/**
	 * This class typically covers URI as follows, it will not validate the URI, it will simply attempt to parse it, failure in providing URI in format mentioned below will lead
	 * to java exception.
	 *
	 * <br/>
	 * <ul>
	 *  <li>jdbc:mysql://${installer:databaseHost}:${installer:databasePort}/${installer:databaseName}?autoReconnectForPools=true&useUnicode=true&characterEncoding=UTF-8</li>
	 *  <li>jdbc:postgresql://${installer:databaseHost}:${installer:databasePort}/${installer:databaseName}</li>
	 *  <li>jdbc:jtds:sqlserver://${installer:databaseHost}:${installer:databasePort}/${installer:databaseName};ssl=request</li>
	 *  <li>jdbc:derby:data;create=true</li>
	 * </ul>
	 */
	static class JDBCURIParser {

		public static final String SCHEME_MYSQL = "mysql";
		public static final String SCHEME_POSTGRESQL = "postgresql";
		public static final String SCHEME_MSSQL = "sqlserver";
		public static final String SCHEME_DERBY = "derby";
		public static final String SCHEME_H2 = "h2";

		public static String SCHEME = "SCHEME";
		public static String HOST = "HOST";
		public static String PORT = "PORT";
		public static String PATH = "PATH";
		public static String DATABASE = "DATABASE";

		public static final String MYSQL_TIMEZONE = "serverTimezone";

		public static Map<String, String> parse(String uri){
			if(StringUtils.isBlank(uri)){
				throw new IllegalArgumentException("Uri cannot be null or blank.");
			}
			if(uri.contains(SCHEME_MYSQL)){
				return parseMySql(uri);
			}else if(uri.contains(SCHEME_POSTGRESQL)){
				return parsePostgres(uri);
			}else if(uri.contains(SCHEME_MSSQL)){
				return parseMsSql(uri);
			}else if(uri.contains(SCHEME_DERBY)){
				return parseDerby(uri);
			}else if(uri.contains(SCHEME_H2)){
				return parseH2(uri);
			}

			return Collections.<String, String>emptyMap();
		}

		private static Map<String, String> parseMySql(String uri){
			String toParse = uri.replaceAll("jdbc:", "");
			Map<String, String> tksn = makeTokens(toParse);
			if(tksn.containsKey(MYSQL_TIMEZONE)) {
				tksn.put(JDBC_TIMEZONE, tksn.get(MYSQL_TIMEZONE));
			}
			return tksn;
		}

		private static Map<String, String> parsePostgres(String uri){
			String toParse = uri.replaceAll("jdbc:", "");
			return makeTokens(toParse);
		}

		private static Map<String, String> parseMsSql(String uri){
			String toParse = uri.replaceAll("jdbc:jtds:", "");
			return makeTokens(toParse);
		}

		private static Map<String, String> parseDerby(String uri){
			String[] parts = uri.split(":");
			//server
			if(parts[2].startsWith("//")){
				String toParse = uri.replaceAll("jdbc:", "");
				return makeTokens(toParse);
			}else{//embedded
				String toParse = cleanUpDerbySubProtocol(uri);
				Map<String, String> tokens = new HashMap<>();
				tokens.put(SCHEME, mapDatabase("derby"));
				tokens.put(HOST, "localhost");
				tokens.put(PORT, "0");
				tokens.put(PATH, "");
				tokens.put(DATABASE, processEmbeddedDerbyDatabase(toParse.split(":")[2]));
				return tokens;
			}
		}
		
		private static Map<String, String> parseH2(String uri){
			String[] parts = uri.split(":");
			//server
			if(parts[2].startsWith("//")){
				String toParse = uri.replaceAll("jdbc:", "");
				return makeTokens(toParse);
			}else{//embedded
				String toParse = cleanUpDerbySubProtocol(uri);
				Map<String, String> tokens = new HashMap<>();
				tokens.put(SCHEME, mapDatabase("h2"));
				tokens.put(HOST, "localhost");
				tokens.put(PORT, "0");
				tokens.put(PATH, "");
				tokens.put(DATABASE, processEmbeddedDerbyDatabase(toParse.split(":")[2]));
				return tokens;
			}
		}

		private static String processEmbeddedDerbyDatabase(String value) {
			return value.split(";")[0];
		}

		private static String mapDatabase(String scheme) {
			switch (scheme) {
				case SCHEME_MYSQL:
					return MYSQL;
				case SCHEME_POSTGRESQL:
					return POSTGRES;
				case SCHEME_MSSQL:
					return MSSQL;
				case SCHEME_DERBY:
					return DERBY;
				case SCHEME_H2:
					return H2;
			}

			return "";
		}

		/**
		 * Clears subprotocol from derby
		 *
		 * directory memory classpath jar
		 * @param uri
		 * @return
		 */
		private static String cleanUpDerbySubProtocol(String uri) {
			return uri.replaceAll("directory:","").replaceAll("memory:","").replaceAll("classpath:", "").replaceAll("jar:","");
		}

		private static Map<String, String> makeTokens(String uri){
			URI uriObj = URI.create(uri);
			Map<String, String> tokens = new HashMap<>();
			tokens.put(SCHEME, mapDatabase(uriObj.getScheme()));
			if(StringUtils.isNotBlank(uriObj.getHost()))
				tokens.put(HOST, uriObj.getHost());
			tokens.put(PORT, mapPort(tokens.get(SCHEME), uriObj.getPort()));
			tokens.put(PATH, uriObj.getPath());
			
			String query = uriObj.getQuery();
			if(StringUtils.isNotBlank(query)) {
				for(String a : query.split("&")) {
					int idx = a.indexOf('=');
					if(idx == -1) {
						tokens.put(a, "");
					}
					else {
						try {
							tokens.put(URLDecoder.decode(a.substring(0, idx), "UTF-8"), URLDecoder.decode(a.substring(idx + 1), "UTF-8"));
						} catch (UnsupportedEncodingException e) {
							throw new IllegalStateException("Failed to decode.", e);
						}
					}
				}
			}
			
			String database = getDatabase(uriObj.getPath());
			if(StringUtils.isNotBlank(database))
				tokens.put(DATABASE, database);
			return tokens;
		}

		private static String mapPort(String database, int port) {
			if(port == -1) {
				switch (database) {
					case MYSQL:
						return "3306";
					case POSTGRES:
						return "5432";
					case MSSQL:
						return "1433";
					case DERBY:
						return "1527";
				}
				return "0";
			}

			return "" + port;
		}

		private static String getDatabase(String path){
			if(path == null) {
				return path;
			}
			else if(path.contains("?")){
				return path.substring(1, path.indexOf("?"));
			}else if(path.contains(":")){
				return path.substring(1, path.indexOf(":"));
			}else if(path.contains(";")){
				return path.substring(1, path.indexOf(";"));
			}else{
				return path.substring(1);
			}
		}
	}

}
