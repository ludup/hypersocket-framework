package com.hypersocket.spring.jconfig;

import java.io.File;
import java.io.IOException;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
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
import org.springframework.core.io.FileSystemResource;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import com.hypersocket.scheduler.AutowiringSpringBeanJobFactory;

@Configuration
public class QuartzSpringConfiguration {
	
	static Logger log = LoggerFactory.getLogger(QuartzSpringConfiguration.class);
	
	private static final String POSTGRES = "postgres";
	private static final String MYSQL = "mysql";//innodb
	//not tested
	private static final String ORACLE = "oracle";
	private static final String MSSQL = "ms";
	private static final String DERBY = "derby";
	
	private static final Map<String, String> databaseScript = new HashMap<>();
	
	static {
		databaseScript.put(POSTGRES, "/conf/quartz-tables-postgres.sql");
		databaseScript.put(MYSQL, "/conf/quartz-tables-mysql.sql");
		databaseScript.put(ORACLE, "/conf/quartz-tables-oracle.sql");
		databaseScript.put(MSSQL, "/conf/quartz-tables-mssql.sql");
		databaseScript.put(DERBY, "/conf/quartz-tables-derby.sql");
	}
	
	@Autowired 
	AutowiringSpringBeanJobFactory autowiringSpringBeanJobFactory;
	
	@Autowired
	DataSource dataSource;
	
	@Autowired
	PlatformTransactionManager transactionManager;
	
	@Autowired
	ApplicationContext applicationContext;
	
	@Autowired
	Environment environment;
	
	@Value("${user.dir}") 
	String userDir; 
	
	@Bean
	SchedulerFactoryBean schedulerFactoryBean(@Qualifier("quartzProperties") Properties quartzProperties) throws SQLException{
		
		checkTables(quartzProperties);
		
		SchedulerFactoryBean quartzScheduler = new SchedulerFactoryBean();
		
		quartzScheduler.setQuartzProperties(quartzProperties);

		quartzScheduler.setDataSource(dataSource);
		quartzScheduler.setTransactionManager(transactionManager);

		quartzScheduler.setJobFactory(autowiringSpringBeanJobFactory);

		quartzScheduler.setAutoStartup(true);
		quartzScheduler.setSchedulerName("quartzScheduler");
		quartzScheduler.setApplicationContext(applicationContext);
		
		return quartzScheduler;
	}
	
	
	@Bean
	@Qualifier("quartzProperties")
	public Properties quartzProperties() {
		PropertiesFactoryBean propertiesFactoryBean = new PropertiesFactoryBean();
		propertiesFactoryBean.setLocation(new FileSystemResource(userDir + "/conf/quartz-ha.properties"));
		Properties properties = null;
		try {
			propertiesFactoryBean.afterPropertiesSet();
			properties = propertiesFactoryBean.getObject();

		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
		
		properties.put("org.quartz.jobStore.isClustered", String.valueOf(environment.acceptsProfiles("HA")));

		return properties;
	}
	
	private void checkTables(Properties quartzProperties) throws SQLException {
		Connection connection = null;
		ResultSet resultSet = null;
		Boolean autoCommitStatus = null;
		Statement statement = null;
		boolean executeScript = true;
		try{
			connection = dataSource.getConnection();
			String databaseProductName = connection.getMetaData().getDatabaseProductName();
			String quartzTablePrefix = quartzProperties.getProperty("org.quartz.jobStore.tablePrefix").toLowerCase();
			autoCommitStatus = connection.getAutoCommit();
			connection.setAutoCommit(false);
			resultSet = connection.getMetaData().getTables(null, null, "%", null);
			while (resultSet.next()) {
				String tableName = resultSet.getString("TABLE_NAME").toLowerCase();
				if(tableName.startsWith(quartzTablePrefix)){
					executeScript = false;
					break;
				}
				
			}
			if(executeScript){
				statement = connection.createStatement();
				
				File file = new File(userDir + getDatabaseFile(databaseProductName));
				log.info(String.format("Reading sql file from location %s", file.getAbsolutePath()));
				
				LineIterator iterator = FileUtils.lineIterator(file, "UTF-8");
				while (iterator.hasNext()) {
					String sql = (String) iterator.next();
					statement.addBatch(sql);
				}
				statement.executeBatch();
				connection.commit();
			}
		}catch(BatchUpdateException e) {
			connection.rollback();
	        log.error(String.format("Quartz script import failed SQLException: %s SQLState: %s Message: %s Vendor error code: %s", e.getMessage()
	        		, e.getSQLState(), e.getMessage(), e.getErrorCode()), e);
	        throw new IllegalStateException(e);
	    }catch(Exception e){
	    	connection.rollback();
	    	log.error("Quartz script import failed !!!", e);
			throw new IllegalStateException(e);
		}finally{
			if(connection != null){
				if(autoCommitStatus != null){
					connection.setAutoCommit(autoCommitStatus);
				}
				connection.close();
			}
		}
		
	}
	
	private String getDatabaseFile(String databaseProductName){
		if(databaseProductName.toLowerCase().contains("mysql")){
			return databaseScript.get(MYSQL);
		}else if(databaseProductName.toLowerCase().contains("postgres")){
			return databaseScript.get(POSTGRES);
		}else if(databaseProductName.toLowerCase().contains("oracle")){
			return databaseScript.get(ORACLE);
		}else if(databaseProductName.toLowerCase().contains("mssql")){
			return databaseScript.get(MSSQL);
		}else if(databaseProductName.toLowerCase().contains("derby")){
			return databaseScript.get(DERBY);
		}
		
		throw new IllegalArgumentException(String.format("Script not found for database %s",databaseProductName));
	}

}
