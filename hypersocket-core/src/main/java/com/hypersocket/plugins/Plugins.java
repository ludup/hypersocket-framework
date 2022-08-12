package com.hypersocket.plugins;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.pf4j.Plugin;
import org.pf4j.PluginClassLoader;
import org.w3c.dom.Document;

import com.hypersocket.json.version.Version;

public class Plugins {

	public static final String X_PLUGIN_NAME = "x.plugin.name";
	public static final String X_PLUGIN_WEIGHT = "x.plugin.weight";
	public static final String X_PLUGIN_MANDATORY = "x.plugin.mandatory";
	public static final String X_PLUGIN_SYSTEM = "x.plugin.system";
	public static final String X_PLUGIN_LICENSE = "x.plugin.license";
	public static final String X_PLUGIN_LICENSE_URL = "x.plugin.licenseUrl";
	public static final String X_PLUGIN_URL = "x.plugin.url";

	private Plugins() {
	}

	public static Properties getPluginProperties(Path path) {
		if (Files.isDirectory(path)) {
			try (var in = Files.newInputStream(path.resolve("plugin.properties"))) {
				return properties(in);
			} catch (IOException ioe) {
				throw new IllegalArgumentException(String.format("Could not open %s as a zip file.", path));
			}
		} else {
			try (var in = Files.newInputStream(path)) {
				return getPluginProperties(in);
			} catch (IOException ioe) {
				throw new IllegalArgumentException(String.format("Could not open %s as a zip file.", path));
			}
		}
	}

	public static Properties getPluginProperties(InputStream in) {
		ZipEntry en;
		try (var z = new ZipInputStream(in)) {
			while ((en = z.getNextEntry()) != null) {
				if (en.getName().equals("plugin.properties")) {
					return properties(z);
				}
			}
		} catch (IOException ioe) {
		}
		throw new IllegalArgumentException("Not a plugin stream.");
	}

	public static Properties getExtensionProperties(Path path) {
		try (var in = Files.newInputStream(path)) {
			return getExtensionProperties(in);
		} catch (IOException ioe) {
			throw new IllegalArgumentException(String.format("Could not open %s as a zip file.", path));
		}
	}

	public static String getExtensionVersionOrDefault(URL path, String defaultVersion) {
		try {
			try (var in = path.openStream()) {
				return getExtensionVersion(in);
			} catch (IOException ioe) {
				throw new IllegalArgumentException(String.format("Could not open %s as a zip file.", path));
			}
		}
		catch(Exception e) {
			return defaultVersion;
		}
	}

	public static String getExtensionVersionOrDefault(Path path, String defaultVersion) {
		try {
			try (var in = Files.newInputStream(path)) {
				return getExtensionVersion(in);
			} catch (IOException ioe) {
				throw new IllegalArgumentException(String.format("Could not open %s as a zip file.", path));
			}
		}
		catch(Exception e) {
			return defaultVersion;
		}
	}

	public static String getExtensionVersion(Path path) {
		try (var in = Files.newInputStream(path)) {
			return getExtensionVersion(in);
		} catch (IOException ioe) {
			throw new IllegalArgumentException(String.format("Could not open %s as a zip file.", path));
		}
	}

	public static Properties getExtensionProperties(InputStream in) {
		ZipEntry en;
		try (var z = new ZipInputStream(in)) {
			while ((en = z.getNextEntry()) != null) {
				if (en.getName().equals("extension.def") || en.getName().endsWith("/extension.def")) {
					return properties(z);
				}
			}
		} catch (IOException ioe) {
		}
		throw new IllegalArgumentException("Not an extension stream.");
	}

	public static Properties getDefaultMavenManifiestProperties(Path path) {
		try (var in = Files.newInputStream(path)) {
			return getDefaultMavenManifiestProperties(in);
		} catch (IOException ioe) {
			throw new IllegalArgumentException(String.format("Could not open %s as a zip file.", path));
		}
	}
	
	public static String getBestProperty(String defaultValue, Collection<Properties> properties, String... keys) {
		var it = Arrays.asList(keys).iterator();
		for(var p : properties) {
			var k = it.hasNext() ? it.next() : "";
			if(!k.equals("") && p.containsKey(k))
				return p.getProperty(k);
		}
		return defaultValue;
	}
	
	public static Properties getDefaultMavenManifiestProperties(InputStream in) {
		var doc = getMavenManifestInsideArchive(in);
		var p = new Properties();
		putIfNotNull(p, "artifactId", "artifactId", doc);
		putIfNotNull(p, "name", "name", doc);
		putIfNotNull(p, "description", "description", doc);
		putIfNotNull(p, "version", "version", doc);
		return p;
	}

	public static Document getMavenManifestInsideArchive(InputStream in) {
		return getMavenManifestInsideArchive(in, null);
	}
	
	private static Document getMavenManifestInsideArchive(InputStream in, String extName) {
		ZipEntry en;
		boolean hasDef = false;
		Document hasVersion = null;
		try (var z = new ZipInputStream(in)) {
			while ((en = z.getNextEntry()) != null) {
				if(extName == null) {
					extName = en.getName();
					while(extName.startsWith("/"))
						extName = extName.substring(1);
					extName = extName.split("/")[0];
				}
				if (en.getName().equals("extension.def") || en.getName().endsWith("/extension.def")) {
					hasDef = true;
				} else if (en.getName().matches(".*META-INF/maven/.*/pom\\.xml")) {
					try {
			    		var docBuilderFactory = DocumentBuilderFactory.newInstance();
			            var docBuilder = docBuilderFactory.newDocumentBuilder();
			            hasVersion = docBuilder.parse (z);
					}
					catch(Exception e) {
						e.printStackTrace();
					}
				} else if (extName != null && en.getName().matches(".*/" + extName + ".*\\.jar")) {
					try {
						return getMavenManifestInsideArchive(z, extName);
					} catch (IllegalArgumentException iae) {
					}
				}
			}
		} catch (IOException ioe) {
		}
		if (hasDef && hasVersion != null) {
			return hasVersion;
		}
		throw new IllegalArgumentException("Not an extension stream.");
	}

	public static String getExtensionVersion(InputStream in) {
		ZipEntry en;
		boolean hasDef = false;
		String hasVersion = null;
		try (var z = new ZipInputStream(in)) {
			while ((en = z.getNextEntry()) != null) {
				if (en.getName().equals("extension.def")) {
					var p = new Properties();
					p.load(z);
					if(p.containsKey("extension.version")) {
						return p.getProperty("extension.version");
					}
					hasDef = true;
				} else if (en.getName().equals("META-INF/MANIFEST.MF")) {
					var mf = new Manifest(z);
					hasVersion = mf.getMainAttributes().getValue("Implementation-Version");
				} else if (en.getName().endsWith(".jar")) {
					try {
						return getExtensionVersion(z);
					} catch (IllegalArgumentException iae) {
					}
				}
			}
		} catch (IOException ioe) {
		}
		if (hasDef && hasVersion != null)
			return hasVersion;
		throw new IllegalArgumentException("Not an extension stream.");
	}

	public static void runInPluginClassLoader(Plugin plugin, Runnable r) {
		var was = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(plugin.getWrapper().getPluginClassLoader());
			r.run();
		} finally {
			Thread.currentThread().setContextClassLoader(was);
		}
	}

	public static <T> T callInPluginClassLoader(Plugin plugin, Callable<T> r) throws Exception {
		var was = Thread.currentThread().getContextClassLoader();
		try {
			Thread.currentThread().setContextClassLoader(plugin.getWrapper().getPluginClassLoader());
			return r.call();
		} finally {
			Thread.currentThread().setContextClassLoader(was);
		}
	}

	public static Iterable<URL> pluginResources(ClassLoader classLoader, String name) {
		return new Iterable<URL>() {

			@Override
			public Iterator<URL> iterator() {
				try {
					var en = findPluginResources(classLoader, name);
					return new Iterator<URL>() {

						@Override
						public boolean hasNext() {
							return en.hasMoreElements();
						}

						@Override
						public URL next() {
							return en.nextElement();
						}

					};
				} catch (IOException ioe) {
					throw new IllegalStateException(String.format("Failed to find resources for '%s'.", ioe));
				}
			}
		};
	}

	public static Enumeration<URL> findPluginResources(ClassLoader classLoader, String name) throws IOException {
		if (classLoader instanceof PluginClassLoader) {
			return ((URLClassLoader) classLoader).findResources(name);
		} else {
			return classLoader.getResources(name);
		}
	}

	private static Properties properties(InputStream in) throws IOException {
		var p = new Properties();
		p.load(in);
		return p;
	}

	public static Version normalizeDevVersion(Version v1) {
		var s = v1.toString();
		if(s.contains("-"))
			return new Version(s.replace("SNAPSHOT", String.valueOf(Integer.MAX_VALUE)));
		else
			return new Version(s + "-" + Integer.MAX_VALUE);
	}
	
	private static void putIfNotNull(Properties p, String k, String n, Document doc) {
		var v = findElString(doc, n);
		if(v != null)
			p.put(k, v);
	}

	private static String findElString(Document doc, String name) {
		try {
			return doc.getDocumentElement().getElementsByTagName(name).item(0).getTextContent();
		}
		catch(Exception e) {
			return null;
		}
	}

	public static boolean isPlugin(Path path) {
		try {
			getPluginProperties(path);
			return true;
		}
		catch(IllegalArgumentException iae) {
			return false;
		}
	}

	public static String extractNameFromFilename(String name) {
		if (name.endsWith(".zip")) {
			name = name.substring(0, name.length() - 4);
		}
		int lastDash = name.lastIndexOf('-');
		if(lastDash == -1)
			throw new IllegalArgumentException("Not a versioned filename.");

		int nextDash = name.lastIndexOf('-', lastDash - 1);
		if(nextDash == -1)
			throw new IllegalArgumentException("Not a versioned filename.");

		return name.substring(0, nextDash);
	}

	public static String extractVersionFromFilename(String name) {
		if (name.endsWith(".zip")) {
			name = name.substring(0, name.length() - 4);
		}
		int lastDash = name.lastIndexOf('-');
		if(lastDash == -1)
			throw new IllegalArgumentException("Not a versioned filename.");

		int nextDash = name.lastIndexOf('-', lastDash - 1);
		if(nextDash == -1)
			throw new IllegalArgumentException("Not a versioned filename.");

		return name.substring(nextDash + 1);
	}
}
