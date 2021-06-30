/*******************************************************************************
 * Copyright (c) 2013 LogonBox Limited.
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
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;
import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.io.Resource;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import com.hypersocket.Version;

public class UpgradeServiceImpl implements UpgradeService, ApplicationContextAware {

	private final static Logger log = LoggerFactory.getLogger(UpgradeServiceImpl.class);

	private Resource[] scripts;
	private final ScriptEngineManager manager;
	private ApplicationContext springContext;
	private List<UpgradeServiceListener> listeners = new ArrayList<UpgradeServiceListener>();
	private String databaseType = null;
	private boolean fresh = true;
	private boolean done = false;

	public UpgradeServiceImpl() {
		manager = new ScriptEngineManager();
	}

	@Override
	public void registerListener(UpgradeServiceListener listener) {
		listeners.add(listener);
	}

	public void setApplicationContext(ApplicationContext springContext) throws BeansException {
		this.springContext = springContext;
	}

	public boolean hasUpgrades() throws IOException {
		return !buildUpgradeOps().isEmpty();
	}

	public final void setScripts(Resource[] scripts) {
		if (log.isInfoEnabled()) {
			log.info("Have upgrade scripts " + Arrays.asList(scripts));
		}
		this.scripts = scripts;
	}

	public String getDatabaseType(Connection connection) {
		if (databaseType != null) {
			return databaseType;
		}
		try {
			DatabaseMetaData metaData = connection.getMetaData();
			databaseType = metaData.getDatabaseProductName();
			if (databaseType.equals("Apache Derby")) {
				databaseType = "derby";
			} else if (databaseType.equals("MySQL")) {
				databaseType = "mysql";
			} else if (databaseType.equals("MariaDB")) {
				databaseType = "mysql";
			}  else if (databaseType.equals("PostgreSQL")) {
				databaseType = "postgres";
			} else if (databaseType.equals("Microsoft SQL Server")) {
				databaseType = "mssql";
			} else if (databaseType.equals("H2")) {
				databaseType = "mysql";
			} else {
				log.info(databaseType + " is not a supported database type");
				databaseType = "unknown";
			}

			return databaseType;
		} catch (SQLException e) {
			log.error("Could not determine database type", e);
		}
		return "unknown";
	}

	public final Resource[] getScripts() {
		return scripts;
	}

	public void preUpgrade(DataSource ds) throws IOException {
		log.info("Doing pre-upgrade ops (before Hibernate is initialized");
		if (log.isInfoEnabled()) {
			log.info("Starting pre-upgrade");
		}

		try (Connection connection = ds.getConnection()) {

			try(ResultSet rs = connection.getMetaData().getTables(ds.getConnection().getCatalog(), null, "upgrade", null)) {
				if(rs.next()) {
					try (Statement st = connection.createStatement()) {
						try (ResultSet rs2 = st.executeQuery("select * from upgrade")) {
							fresh = !rs2.next();
						}
					}		
				}
				else
					fresh = true;
			}
			

			if (fresh) {
				log.info("Database is fresh, not doing any pre-upgrading");
				return;
			} else {
				log.info("Pre-upgrading existing database");
			}
			
			List<UpgradeOp> ops = buildUpgradeOps();
			Map<String, Object> beans = new HashMap<String, Object>();

			// Do all the SQL upgrades first
			try {
				connection.setAutoCommit(false);
				doOps(connection, ops, beans, "pre.sql", "pre." + getDatabaseType(connection));
				connection.commit();
			} catch (IOException | ScriptException e2) {
				connection.rollback();
				throw e2;
			} finally {
				connection.setAutoCommit(true);
			}

			if (log.isInfoEnabled()) {
				log.info("Finished pre-upgrade");
			}

		} catch (SQLException | ScriptException e) {
			throw new IOException("Failed to get pre-upgrade.", e);
		}

	}

	public void upgrade(SessionFactory sessionFactory, TransactionTemplate txnTemplate) {
		try {
			txnTemplate.execute(new TransactionCallback<Object>() {
				public Object doInTransaction(TransactionStatus status) {

					try {
						if (log.isInfoEnabled()) {
							log.info("Starting upgrade");
						}

						if (log.isInfoEnabled()) {
							if (fresh) {
								log.info("Database is fresh");
							} else {
								log.info("Upgrading existing database");
							}
						}
						List<UpgradeOp> ops = buildUpgradeOps();
						Map<String, Object> beans = new HashMap<String, Object>();

						// Do all the SQL upgrades first
						sessionFactory.getCurrentSession().doWork((connection) -> {
							try {
								doOps(connection, ops, beans, "sql", getDatabaseType(connection), "js", "class");
								

								// Also mark all the pre.sql ops as done now we definitely have an upgrade table
								List<String> l = Arrays.asList("pre.sql", "pre." + getDatabaseType(connection));
								for (UpgradeOp op : ops) {
									if (l.contains(op.getLanguage())) {
										Upgrade upgrade = getUpgrade(connection, op.getModule(), op.getLanguage());
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
											if (log.isInfoEnabled()) {
												log.info("Pre-Module " + op.getModule() + "/" + op.getLanguage() + " is now at " + op.getVersion());
											}
											upgrade.setVersion(op.getVersion().toString());
											saveOrUpdate(connection, upgrade);
										} else {
											if (log.isInfoEnabled()) {
												log.info("Pre-Module " + op.getModule() + "/" + op.getLanguage() + " is at version "
														+ currentVersion + ", no need to run script for " + op.getVersion());
											}
										}
									}
								}
								
								
								
							} catch (IOException | ScriptException ioe) {
								throw new IllegalStateException("Failed to run upgrade scripts.", ioe);
							}

							for (UpgradeServiceListener listener : listeners) {
								listener.onUpgradeFinished();
							}
						});

						if (log.isInfoEnabled()) {
							log.info("Finished upgrade");
						}
					} catch (Throwable e) {
						log.error("Failed to upgrade", e);
						throw new IllegalStateException("Errors upgrading database");
					}
					return null;
				}
			});
		} catch (TransactionException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}

		if (log.isInfoEnabled()) {
			log.info("Completed upgrade");
		}

		for (UpgradeServiceListener listener : listeners) {
			listener.onUpgradeComplete();
		}

		done = true;

	}

	@Override
	public boolean isDone() {
		return done;
	}

	protected void doOps(Connection connection, List<UpgradeOp> ops, Map<String, Object> beans, String... languages)
			throws ScriptException, IOException {
		List<String> l = Arrays.asList(languages);
		for (UpgradeOp op : ops) {
			if (l.contains(op.getLanguage())) {
				Upgrade upgrade = getUpgrade(connection, op.getModule(), op.getLanguage());
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
					if (log.isInfoEnabled()) {
						log.info("Module " + op.getModule() + "/" + op.getLanguage() + " is currently at version "
								+ currentVersion + ". Running the script " + op.getUrl()
								+ " will take the module to version " + op.getVersion());
					}
					executeScript(connection, beans, op.getUrl());
					if (log.isInfoEnabled()) {
						log.info("Module " + op.getModule() + "/" + op.getLanguage() + " is now at " + op.getVersion());
					}
					upgrade.setVersion(op.getVersion().toString());
					saveOrUpdate(connection, upgrade);
				} else {
					if (log.isInfoEnabled()) {
						log.info("Module " + op.getModule() + "/" + op.getLanguage() + " is at version "
								+ currentVersion + ", no need to run script for " + op.getVersion());
					}
				}
			}
		}
	}

	private void saveOrUpdate(Connection connection, Upgrade upgrade) throws IOException {
		try (PreparedStatement st = connection.prepareStatement(
				"insert into upgrade (language,module,version) values (?,?,?) on duplicate key update version = ?")) {
			st.setString(1, upgrade.getPk().getLanguage());
			st.setString(2, upgrade.getPk().getModule());
			st.setString(3, upgrade.getVersion());
			st.setString(4, upgrade.getVersion());
			st.executeUpdate();
		} catch (SQLException e) {
			throw new IOException("Failed to get upgrade.", e);
		}

	}

	private Upgrade getUpgrade(Connection connection, String module, String language) throws IOException {
		try (PreparedStatement st = connection
				.prepareStatement("select * from upgrade where module = ? and language = ?")) {
			st.setString(1, module);
			st.setString(2, language);
			try (ResultSet rs = st.executeQuery()) {
				if (rs.next())
					return new Upgrade(rs.getString("version"), module, language);
				else
					return null;
			}
		} catch (SQLException e) {
			throw new IOException("Failed to get upgrade.", e);
		}
	}

	private List<UpgradeOp> buildUpgradeOps() throws IOException {
		List<UpgradeOp> ops = new ArrayList<UpgradeOp>();
		for (Resource script : scripts) {
			URL url = script.getURL();

			url.getFile();

			String name = url.getPath();
			int idx;
			if ((idx = name.lastIndexOf("/")) > -1) {
				name = name.substring(idx + 1);
			}
			
			// Skip if this looks like an inner class
			if (name.indexOf('$') != -1)
				continue;

			// Java identifiers (i.e. .java upgrade 'scripts') may only contain
			// _ and currency symbols. So we use $ for a dash (-) and £ for dot
			// (.) as underscore is already used
			name = name.replace("_DASH_", "-");
			name = name.replace("_DOT_", ".");

			int moduleIdx = name.indexOf('_');
			String moduleName = name.substring(0, moduleIdx);
			name = name.substring(moduleIdx + 1);
			idx = name.lastIndexOf('.');
			String modname = name.substring(0, idx);
			String modlang;
			if(modname.endsWith(".pre")) {
				modname = modname.substring(0, modname.length() - 4);
				modlang = "pre." + name.substring(idx + 1);
			}
			else
				modlang = name.substring(idx + 1);
			ops.add(new UpgradeOp(new Version(modname), url, moduleName, modlang));
		}
		Collections.sort(ops);
		return ops;
	}

	private void executeScript(Connection connection, Map<String, Object> beans, URL script)
			throws ScriptException, IOException {
		if (log.isInfoEnabled()) {
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
				Class<? extends Runnable> clazz = (Class<? extends Runnable>) getClass().getClassLoader()
						.loadClass(path);
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
					if (line.toUpperCase().startsWith("EXIT IF FRESH")) {
						if (isFreshInstall()) {
							break;
						}
						continue;
					}
					if (line.toUpperCase().startsWith("TRY")) {
						ignoreErrors = true;
						continue;
					}
					if (line.toUpperCase().startsWith("CATCH")) {
						ignoreErrors = false;
						continue;
					}

					if (!line.startsWith("/*") && !line.startsWith("//")) {
						if (line.endsWith(";")) {
							line = line.substring(0, line.length() - 1);
							statement += line;

							executeStatement(connection, statement, ignoreErrors);
							statement = "";
						} else {
							statement += line + "\n";
						}
					}
				}

				if (StringUtils.isNotBlank(statement)) {
					executeStatement(connection, statement, ignoreErrors);
				}
			} catch (Exception e) {
				if (e instanceof IOException)
					throw (IOException) e;
				else
					throw new IOException("Failed upgrade.", e);
			} finally {
				r.close();
			}
		}

	}

	public void executeStatement(Connection c, String statement, boolean ignoreErrors) throws Exception {
		statement = statement.trim();
		if (statement.toUpperCase().startsWith("DROP ALL FOREIGN KEYS")) {
			List<String> queries = new ArrayList<>();
			if (statement.toUpperCase().startsWith("DROP ALL FOREIGN KEYS FOR ")) {
				String tableName = statement.substring(26).trim();
				DatabaseMetaData dm = c.getMetaData();
				try (ResultSet rs = dm.getImportedKeys(null, null, tableName)) {
					while (rs.next()) {
						queries.add(
								String.format("ALTER TABLE %s DROP FOREIGN KEY %s", tableName, rs.getString("FK_NAME")));
					}
				}				
			}
			else {
				try (ResultSet rs = c.getMetaData().getTables(c.getCatalog(), null, null, new String[] { "TABLE" })) {
					while (rs.next()) {
						String tn = rs.getString("TABLE_NAME");
						if (!tn.toLowerCase().startsWith("qrtz_") && !tn.toLowerCase().equals("c3p0_test_table")
								&& !tn.toLowerCase().equals("hibernate_sequences")) {
							ResultSet rs2 = c.getMetaData().getImportedKeys(null, null, tn);
							Set<String> done = new HashSet<>();
							while (rs2.next()) {
								String keyn = rs2.getString("FK_NAME");
								if (!done.contains(keyn)) {
									done.add(keyn);
									queries.add(String.format("ALTER TABLE %s DROP FOREIGN KEY %s", tn, keyn));
								}
							}
						}
					}
				}
			}
			
			for (String q : queries) {
				try {
					log.info("SQL: " + q);
					try (Statement s = c.createStatement()) {
						s.executeUpdate(q);
					}
				} catch (Exception e) {
					if (!ignoreErrors) {
						throw e;
					}
				}
			}
		} else {
			try {
				log.info("SQL: " + statement);
				try (Statement s = c.createStatement()) {
					s.executeUpdate(statement);
				}
			} catch (Exception e) {
				if (!ignoreErrors) {
					throw e;
				}
			}
		}
	}

	@Override
	public boolean isFreshInstall() {
		return fresh;
	}

	private ScriptEngine buildEngine(Map<String, Object> beans, URL script, ScriptContext context)
			throws ScriptException, IOException {
		// Create a new scope for the beans
		Bindings engineScope = context.getBindings(ScriptContext.ENGINE_SCOPE);
		for (String key : beans.keySet()) {
			engineScope.put(key, beans.get(key));
		}
		engineScope.put("ctx", springContext);

		// Execute the script
		String name = script.getPath();
		int idx;
		if ((idx = name.indexOf("/")) > -1) {
			name = name.substring(idx + 1);
		}
		if ((idx = name.lastIndexOf(".")) > -1) {
			name = name.substring(idx + 1);
		}
		ScriptEngine engine = manager.getEngineByExtension(name);
		if (engine == null) {
			throw new IOException("No scripting engine found for " + name);
		}
		return engine;
	}

}
