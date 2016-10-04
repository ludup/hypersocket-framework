package com.hypersocket.util;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInformation {
	
	private Set<String> tablesOnStartUp = new HashSet<>();

	@Autowired DataSource dataSource;
	String ormOnOld = null;
	
	@PostConstruct
	public void postConstruct() throws SQLException{
		probeTables(tablesOnStartUp);
		if(tablesOnStartUp.contains("properties")){
			probeOrmOnOld();
		}
	}

	private void probeOrmOnOld() throws SQLException {
		Connection connection = null;
		Statement statement = null;
		ResultSet rs = null;
		try {
			connection = dataSource.getConnection();
			statement = connection.createStatement();
			rs = statement.executeQuery("select value from properties where resourceKey = 'orm.on.old'");
			while(rs.next()){
				ormOnOld = rs.getString("value");
			} 
		} finally{
			if(rs != null){
				rs.close();
			}
			if(connection != null){
				connection.close();
			}
		}
		
	}

	private void probeTables(Set<String> tables) throws SQLException {
		Connection connection = null;
		ResultSet rs = null;
		try {
			connection = dataSource.getConnection();
			rs = connection.getMetaData().getTables(null, null, "%", null);
			while(rs.next()){
				tables.add(rs.getString("TABLE_NAME").toLowerCase());
			} 
		} finally{
			if(rs != null){
				rs.close();
			}
			if(connection != null){
				connection.close();
			}
		}
		
	}
	
	/**
	 * These values are populated prior to Hibernate Schema generation, on start up.
	 * Once application is up, this value might be not in synch, it is to record start up state only.
	 * 
	 * @return
	 */
	public Set<String> getTablesOnStartUp(){
		return tablesOnStartUp;
	}
	
	/**
	 * This value is fetched before any CRUD or upgrade operation, on start up.
	 * Once application is up, this value might be not in synch, it is to record start up state only.
	 * 
	 * @return
	 */
	public String getOrmOnOld(){
		return this.ormOnOld;
	}
	
	/**
	 * Empty database on startup will have single entry of c3p0_test_table
	 * 
	 * @return
	 */
	public boolean isClean(){
		return this.tablesOnStartUp.size() == 1 && this.tablesOnStartUp.contains("c3p0_test_table");
	}
}
