package com.hypersocket.extensions;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Encapsulates a list of extension archives, and the URLs of all the
 * extension.def resources that are contained within.
 * <p>
 * A single application may have more than one 'place' (in the case of the HS
 * Client, the service extensions are stored in one location, and the GUI
 * extensions are stored in another).
 */
public class ExtensionPlace implements Serializable {
	private static final long serialVersionUID = 1L;

	final static Logger LOG = LoggerFactory.getLogger(ExtensionPlace.class.getName());

	private Map<String, File> bootstrapArchives;
	private List<URL> urls;
	private File dir;
	private String app;
	private boolean downloadAllExtensions;

	public ExtensionPlace(String app, File dir) {
		// Constructor for extension places that are not loaded (e.g. client updates hosted on HS server)
		this(app, dir, new HashMap<String, File>(), new ArrayList<URL>());
	}

	public ExtensionPlace(String app, File dir,
			Map<String, File> bootstrapArchives, List<URL> urls) {
		super();
		this.app = app;
		this.dir = dir;
		this.bootstrapArchives = bootstrapArchives;
		this.urls = urls;
	}

	public boolean isDownloadAllExtensions() {
		return downloadAllExtensions;
	}

	public void setDownloadAllExtensions(boolean downloadAllExtensions) {
		this.downloadAllExtensions = downloadAllExtensions;
	}

	public String getApp() {
		return app;
	}

	public File getDir() {
		return dir;
	}

	public Map<String, File> getBootstrapArchives() {
		return bootstrapArchives;
	}

	public List<URL> getUrls() {
		return urls;
	}

	/**
	 * Get the default extension place. This consists of extensions archives for
	 * the application hosted by this JVM, and any extensions it has loaded.
	 */
	public static ExtensionPlace getDefault() {
		List<URL> u = new ArrayList<URL>();
		try {
			Enumeration<URL> urls = ( Thread.currentThread().getContextClassLoader() == null ? ExtensionDefinition.class.getClassLoader() : Thread.currentThread().getContextClassLoader() )
					.getResources("extension.def");
			if (!urls.hasMoreElements()) {
				if (LOG.isInfoEnabled()) {
					LOG.info("No extension.def resources could be found on the classpath");
				}
				urls = ExtensionDefinition.class.getClassLoader().getResources(
						"/extension.def");
			}
			while (urls.hasMoreElements())
				u.add(urls.nextElement());
			String id = System.getProperty("hypersocket.id", "hypersocket-vpn");
			if(id == null) {
				throw new RuntimeException("System property hypersocket.id must be set for extension to function.");
			}
			return new ExtensionPlace(id,
					new File(System.getProperty(
							"hypersocket.bootstrap.archivesDir", "gui")),
					ExtensionHelper.getBootstrapArchives(), u);
		} catch (IOException ioe) {
			throw new RuntimeException(
					"Could not load extension definitions for default extensions directory.");
		}
	}

}
