package com.hypersocket.spring.jconfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import com.hypersocket.scheduler.AutowiringSpringBeanJobFactory;
import com.hypersocket.util.DatabaseInformation;
import com.hypersocket.utils.HypersocketUtils;

@Configuration
public class QuartzSpringConfiguration {
	
	static Logger log = LoggerFactory.getLogger(QuartzSpringConfiguration.class);
	
	private static final String POSTGRES = "postgres";
	private static final String MYSQL = "mysql";
	private static final String MARIADB = "MariaDB";
	private static final String H2 = "h2";
	
	private static final String ORACLE = "oracle";
	private static final String MSSQL = "ms";
	private static final String DERBY = "derby";
	
	private static final Map<String, String> databaseScript = new HashMap<>();
	
	static {
		databaseScript.put(POSTGRES, "/conf/quartz-tables-postgres.sql");
		databaseScript.put(MYSQL, "/conf/quartz-tables-mysql.sql");
		databaseScript.put(MARIADB, "/conf/quartz-tables-mysql.sql");
		databaseScript.put(H2, "/conf/quartz-tables-h2.sql");
		databaseScript.put(ORACLE, "/conf/quartz-tables-oracle.sql");
		databaseScript.put(MSSQL, "/conf/quartz-tables-mssql.sql");
		databaseScript.put(DERBY, "/conf/quartz-tables-derby.sql");
	}
	
	@Autowired 
	private AutowiringSpringBeanJobFactory autowiringSpringBeanJobFactory;
	
	@Autowired
	private DataSource dataSource;
	
	@Autowired
	private PlatformTransactionManager transactionManager;
	
	@Autowired
	private ApplicationContext applicationContext;
	
	@Autowired
	private Environment environment;
	
	@Autowired
	private DatabaseInformation databaseInformation;
	
	@Value("${user.dir}") 
	private String userDir; 
	
	@Bean
	SchedulerFactoryBean schedulerFactoryBean(@Qualifier("quartzProperties") Properties quartzProperties) throws SQLException{
		
		checkTables(quartzProperties);
		
		SchedulerFactoryBean quartzScheduler = new SchedulerFactoryBean();
		
		quartzScheduler.setQuartzProperties(quartzProperties);

		quartzScheduler.setDataSource(dataSource);
		quartzScheduler.setTransactionManager(transactionManager);

		quartzScheduler.setJobFactory(autowiringSpringBeanJobFactory);

		quartzScheduler.setAutoStartup(false);
		quartzScheduler.setSchedulerName("quartzScheduler");
		quartzScheduler.setApplicationContext(applicationContext);
		
		return quartzScheduler;
	}
	
	
	@Bean
	@Qualifier("quartzProperties")
	public Properties quartzProperties() {
		PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
		propertiesFactoryBean.setLocation(new ClassPathResource("/conf/quartz.properties"));
		Properties properties = null;
		try {
			propertiesFactoryBean.afterPropertiesSet();
			properties = propertiesFactoryBean.getObject();

		} catch (IOException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
		
		File quartzProperties = new File(HypersocketUtils.getConfigDir(), "quartz.properties");
		if(quartzProperties.exists()) {
			try(InputStream in = new FileInputStream(quartzProperties)) {
				properties.load(in);
			}
			catch(IOException ioe) {
				throw new IllegalStateException("Failed to load custom quartz properties.");
			}
		}
		
		if("0".equals(properties.get("org.quartz.threadPool.threadCount"))) {
			/* max(2,C / 2) for product, or just a fixed 2 for development*/
			if(Boolean.getBoolean("hypersocket.development")) {
				properties.put("org.quartz.threadPool.threadCount", "2");
			}
			else {
				properties.put("org.quartz.threadPool.threadCount", String.valueOf(Math.max(2, Runtime.getRuntime().availableProcessors() / 2)));
			}
		}
		
		properties.put("org.quartz.jobStore.isClustered", String.valueOf(environment.acceptsProfiles("HA")));

		return properties;
	}
	
	private void checkTables(Properties quartzProperties) throws SQLException {
		Connection connection = null;
		Boolean autoCommitStatus = null;
		Statement statement = null;
		boolean executeScript = true;
		InputStream inputStreamOfDatabaseFile = null;
		try{
			connection = dataSource.getConnection();
			String databaseProductName = connection.getMetaData().getDatabaseProductName();
			if(databaseProductName.toLowerCase().contains("postgres")){
				quartzProperties.setProperty("org.quartz.jobStore.driverDelegateClass", "com.hypersocket.quartz.HypersocketPostgresSQLDelegate");
			}
			String quartzTablePrefix = quartzProperties.getProperty("org.quartz.jobStore.tablePrefix").toLowerCase();
			autoCommitStatus = connection.getAutoCommit();
			connection.setAutoCommit(false);
			for(String tableName : databaseInformation.getTablesOnStartUp()) {
				if(tableName.startsWith(quartzTablePrefix)){
					executeScript = false;
					break;
				}
				
			}

			if(executeScript){
				statement = connection.createStatement();
				
				String databaseFile = getDatabaseFile(databaseProductName);
				inputStreamOfDatabaseFile = new ClassPathResource(databaseFile).getInputStream();
				log.info(String.format("Reading sql file from location %s", databaseFile));
				
				LineIterator iterator = IOUtils.lineIterator(inputStreamOfDatabaseFile, "UTF-8");
				StringBuilder s = new StringBuilder();
				while (iterator.hasNext()) {
					String sql = (String) iterator.next();
					if(s.length() > 0) {
						s.append(" ");
					}
					s.append(sql);
					if(sql.trim().endsWith(";")) {
						statement.addBatch(s.toString());
						s.setLength(0);
					}
				}
				statement.executeBatch();
				connection.commit();
			}
		}catch(BatchUpdateException e) {
			if(connection != null){
				connection.rollback();
			}
	        log.error(String.format("Quartz script import failed SQLException: %s SQLState: %s Message: %s Vendor error code: %s", e.getMessage()
	        		, e.getSQLState(), e.getMessage(), e.getErrorCode()), e);
	        throw new IllegalStateException(e.getMessage(), e);
	    }catch(Exception e){
	    	connection.rollback();
	    	log.error("Quartz script import failed !!!", e);
			throw new IllegalStateException(e.getMessage(), e);
		}finally{
			if(connection != null){
				if(autoCommitStatus != null){
					connection.setAutoCommit(autoCommitStatus);
				}
				connection.close();
			}
			
			if(inputStreamOfDatabaseFile != null){
				IOUtils.closeQuietly(inputStreamOfDatabaseFile);
			}
		}
		
	}
	
	private String getDatabaseFile(String databaseProductName){
		if(databaseProductName.toLowerCase().contains("mysql")){
			return databaseScript.get(MYSQL);
		}else if(databaseProductName.toLowerCase().contains("postgres")){
			return databaseScript.get(POSTGRES);
		}else if(databaseProductName.toLowerCase().contains("h2")){
			return databaseScript.get(H2);
		}else if(databaseProductName.toLowerCase().contains("oracle")){
			return databaseScript.get(ORACLE);
		}else if(databaseProductName.toLowerCase().contains("mssql")){
			return databaseScript.get(MSSQL);
		}else if(databaseProductName.toLowerCase().contains("derby")){
			return databaseScript.get(DERBY);
		}else if(databaseProductName.toLowerCase().contains("mariadb")){
			return databaseScript.get(MARIADB);
		}
		
		throw new IllegalArgumentException(String.format("Script not found for database %s",databaseProductName));
	}

}
