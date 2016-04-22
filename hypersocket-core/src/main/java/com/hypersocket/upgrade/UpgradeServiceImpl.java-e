/*******************************************************************************
 * Copyright (c) 2013 Hypersocket Limited.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 ******************************************************************************/
package com.hypersocket.upgrade;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;

import com.hypersocket.Version;

public class UpgradeServiceImpl implements UpgradeService, ApplicationContextAware {

	private final static Logger log = LoggerFactory.getLogger(UpgradeServiceImpl.class);

	private Resource[] scripts;
	private final ScriptEngineManager manager;
	private ApplicationContext springContext;
	private SessionFactory sessionFactory;
	
	List<UpgradeServiceListener> listeners = new ArrayList<UpgradeServiceListener>();
	
	String databaseType = null;
	boolean fresh = true;
	
	public UpgradeServiceImpl() {
		manager = new ScriptEngineManager();
	}

	@Override
	public void registerListener(UpgradeServiceListener listener) {
		listeners.add(listener);
	}
	
	@Autowired
	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}
	
	public void setApplicationContext(ApplicationContext springContext) throws BeansException {
		this.springContext = springContext;
	}

	public boolean hasUpgrades() throws IOException {
		return !buildUpgradeOps().isEmpty();
	}

	public final void setScripts(Resource[] scripts) {
		if(log.isInfoEnabled()) {
			log.info("Have upgrade scripts " + Arrays.asList(scripts));
		}
		this.scripts = scripts;
	}
	
	public String getDatabaseType() {
		if(databaseType!=null) {
			return databaseType;
		}
		try {
			@SuppressWarnings("deprecation")
			Connection connection = sessionFactory.getCurrentSession().connection();
			DatabaseMetaData metaData = connection.getMetaData();
			databaseType = metaData.getDatabaseProductName();
			if(databaseType.equals("Apache Derby")) {
				return databaseType = "derby";
			} else if(databaseType.equals("MySQL")) {
				return databaseType = "mysql";
			} else if(databaseType.equals("PostgreSQL")) {
				return databaseType = "postgres";
			} else if(databaseType.equals("Microsoft SQL Server")) {
				return databaseType = "mssql";
			} else {
				log.info(databaseType + " is not a supported database type");
			}
		} catch (HibernateException e) {
			log.error("Could not determine database type", e);
		} catch (SQLException e) {
			log.error("Could not determine database type", e);
		}
		return "unknown";
	}

	public final Resource[] getScripts() {
		return scripts;
	}

	public void upgrade() throws IOException, ScriptException {
		
		if(log.isInfoEnabled()) {
			log.info("Starting upgrade");
		}
		fresh = sessionFactory.getCurrentSession()
				.createCriteria(Upgrade.class).list().size() == 0;
		
		if(log.isInfoEnabled()) {
			if(fresh) {
				log.info("Database is fresh");
			} else {
				log.info("Upgrading existing database");
			}
		}
		List<UpgradeOp> ops = buildUpgradeOps();
		Map<String, Object> beans = new HashMap<String, Object>();
		
		// Do all the SQL upgrades first
		doOps(ops, beans, "sql", getDatabaseType());

		doOps(ops, beans, "js", "class");
		
		for(UpgradeServiceListener listener : listeners) {
			listener.onUpgradeComplete();
		}
	}

	protected void doOps(List<UpgradeOp> ops, Map<String, Object> beans, String... languages) throws ScriptException, IOException {
		List<String> l = Arrays.asList(languages);
		for (UpgradeOp op : ops) {
			if (l.contains(op.getLanguage())) {
				Upgrade upgrade = getUpgrade(op.getModule(), op.getLanguage());
				if (upgrade == null) {
					String v = "0.0.0";
					if (op.getLanguage().equals("java")) {
						v = "0.0.9";
					}
					upgrade = new Upgrade(v, op.getModule(), op.getLanguage());
				}
				Version currentVersion = new Version(upgrade.getVersion());
				// If version for the script found is greater than the current
				// version then run the script
				if (op.getVersion().compareTo(currentVersion) > 0) {
					if(log.isInfoEnabled()) {
						log.info("Module " + op.getModule() + "/" + op.getLanguage() + " is currently at version " + currentVersion
							+ ". Running the script " + op.getUrl() + " will take the module to version " + op.getVersion());
					}
					executeScript(beans, op.getUrl());
					if(log.isInfoEnabled()) {
						log.info("Module " + op.getModule() + "/" + op.getLanguage() + " is now at " + op.getVersion());
					}
					upgrade.setVersion(op.getVersion().toString());
					sessionFactory.getCurrentSession().saveOrUpdate(upgrade);
				} else {
					if(log.isInfoEnabled()) {
						log.info("Module " + op.getModule() + "/" + op.getLanguage() + " is at version " + currentVersion
							+ ", no need to run script for " + op.getVersion());
					}
				}
			}
		}
	}

	private Upgrade getUpgrade(String module, String language) {
		return (Upgrade) sessionFactory.getCurrentSession().get(Upgrade.class, new UpgradePk(module, language));
	}

	private List<UpgradeOp> buildUpgradeOps() throws IOException {
		List<UpgradeOp> ops = new ArrayList<UpgradeOp>();
		for (Resource script : scripts) {
			URL url = script.getURL();

			url.getFile();
			
			String name = url.getPath();
			int idx;
			if((idx = name.lastIndexOf("/")) > -1) {
				name = name.substring(idx+1);
			}

			// Java identifiers (i.e. .java upgrade 'scripts') may only contain
			// _ and currency symbols. So we use $ for a dash (-) and £ for dot
			// (.) as underscore is already used
			name = name.replace("_DASH_", "-");
			name = name.replace("_DOT_", ".");

			int moduleIdx = name.indexOf('_');
			String moduleName = name.substring(0, moduleIdx);
			name = name.substring(moduleIdx + 1);
			idx = name.lastIndexOf('.');
			ops.add(new UpgradeOp(new Version(name.substring(0, idx)), url, moduleName, name.substring(idx + 1)));
		}
		Collections.sort(ops);
		return ops;
	}

	private void executeScript(Map<String, Object> beans, URL script) throws ScriptException, IOException {
		if(log.isInfoEnabled()) {
			log.info("Executing script " + script);
		}
		
		if (script.getPath().endsWith(".js")) {
			ScriptContext context = new SimpleScriptContext();
			ScriptEngine engine = buildEngine(beans, script, context);
			InputStream openStream = script.openStream();
			if (openStream == null) {
				throw new FileNotFoundException("Could not locate resource " + script);
			}
			Reader in = new InputStreamReader(openStream);
			try {
				engine.eval(in, context);
			} finally {
				in.close();
			}
		} else if (script.getPath().endsWith(".class")) {
			String path = script.getPath();
			int idx = path.indexOf("upgrade/");
			path = path.substring(idx);
			idx = path.lastIndexOf(".");
			path = path.substring(0, idx).replace("/", ".");
			try {
				@SuppressWarnings("unchecked")
				Class<? extends Runnable> clazz = (Class<? extends Runnable>) getClass().getClassLoader().loadClass(path);
				Runnable r = clazz.newInstance();
				springContext.getAutowireCapableBeanFactory().autowireBean(r);
				r.run();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} else {

			BufferedReader r = new BufferedReader(new InputStreamReader(script.openStream()));
			try {
				String statement = "";
				String line;
				boolean ignoreErrors = false;
				while ((line = r.readLine()) != null) {
					line = line.trim();
					if(line.startsWith("EXIT IF FRESH")) {
						if(isFreshInstall()) {
							break;
						}
						continue;
					} 
					if(line.startsWith("TRY")) {
						ignoreErrors = true;
						continue;
					}
					if(line.startsWith("CATCH")) {
						ignoreErrors = false;
						continue;
					}
					
					if(!line.startsWith("/*") && !line.startsWith("//")) {
						if(line.endsWith(";")) {
							line = line.substring(0, line.length()-1);
							statement += line;
							try {
								sessionFactory.getCurrentSession().createSQLQuery(statement).executeUpdate();
							} catch (Throwable e) {
								if(!ignoreErrors) {
									throw e;
								}
							} 
							statement = "";
						} else {
							statement += line + "\n";
						}
					} 
				}
				
				if(StringUtils.isNotBlank(statement)) {
					try {
						sessionFactory.getCurrentSession().createSQLQuery(statement).executeUpdate();
					} catch (Throwable e) {
						if(!ignoreErrors) {
							throw e;
						}
					} 
				}
			} finally {
				r.close();
			}
		}

	}

	@Override
	public boolean isFreshInstall() {
		return fresh;
	}
	
	private ScriptEngine buildEngine(Map<String, Object> beans, URL script, ScriptContext context) throws ScriptException,
			IOException {
		// Create a new scope for the beans
		Bindings engineScope = context.getBindings(ScriptContext.ENGINE_SCOPE);
		for (String key : beans.keySet()) {
			engineScope.put(key, beans.get(key));
		}
		engineScope.put("ctx", springContext);

		// Execute the script
		String name = script.getPath();
		int idx;
		if((idx = name.indexOf("/")) > -1) {
			name = name.substring(idx+1);
		}
		if((idx=name.lastIndexOf(".")) > -1) {
			name = name.substring(idx+1);
		}
		ScriptEngine engine = manager.getEngineByExtension(name);
		if (engine == null) {
			throw new IOException("No scripting engine found for " + name);
		}
		return engine;
	}

}
