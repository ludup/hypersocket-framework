package com.hypersocket.extensions;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.bind.annotation.XmlRootElement;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import com.hypersocket.Version;

@XmlRootElement
public class ExtensionDefinition implements Comparable<ExtensionDefinition>{

	static Logger log = LoggerFactory.getLogger(ExtensionDefinition.class);
	
	String id;
	String license;
	String licenseUrl;
	String image;
	String vendor;
	String url;
	Integer weight;
	String hash;
	File archiveFile;
	List<String> dependencies = new ArrayList<String>();
	long lastModified;
	long size;
	ExtensionState state = ExtensionState.NOT_INSTALLED;
	String remoteDefinitionUrl = null;
	String remoteArchitveUrl = null;
	Long remoteArchiveSize = null;
	Properties remoteProperties;
	boolean isSystem;
	String version;
	
	public ExtensionDefinition() {
		
	}
	
	ExtensionDefinition(String id, String license, String licenseUrl, String depends, long lastModified, String image, String vendor, String url, int weight, long size, String hash, boolean isSystem, String version) {
		this.id = id;
		this.license = license;
		this.licenseUrl = licenseUrl;
		this.lastModified = lastModified;
		this.image = image;
		this.vendor = vendor;
		this.url = url;
		this.weight = weight;
		this.size = size;
		this.hash = hash;
		this.version = version;
		this.isSystem = System.getProperty("hypersocket.bootstrap.systemArchive", "server-core").equals(id) || isSystem;
		StringTokenizer t = new StringTokenizer(depends, ",");
		while(t.hasMoreTokens()) {
			dependencies.add(t.nextToken());
		}
	}
	
	ExtensionDefinition(Properties props, long lastModified, File archiveFile, String version) {
		this(props.getProperty("extension.id"), 
				props.getProperty("extension.license"),
				props.getProperty("extension.licenseUrl"),
				props.getProperty("extension.depends"),
				lastModified / 1000,
				props.getProperty("extension.image"),
				props.getProperty("extension.vendor"),
				props.getProperty("extension.url"),
				Integer.parseInt(props.getProperty("extension.weight")),
				archiveFile == null ? 0 : archiveFile.length(),
				props.getProperty("hash"),
				Boolean.parseBoolean(props.getProperty("extension.system", "false")),
				version);
		this.archiveFile = archiveFile;
	}

	ExtensionDefinition(Element node, String remoteDefinitionBase, String remoteVersion) throws IOException {
		this(node.getAttribute("id"), 
				node.getAttribute("license"),
				node.getAttribute("licenseUrl"),
				node.getAttribute("depends"),
				Long.parseLong(node.getAttribute("lastModified")),
				node.getAttribute("image"),
				node.getAttribute("vendor"),
				node.getAttribute("url"),
				Integer.parseInt(node.getAttribute("weight")),
				Long.parseLong(node.getAttribute("size")),
				node.getAttribute("hash"),
				Boolean.parseBoolean(node.getAttribute("system")),
				remoteVersion);
		remoteProperties = new Properties();
		this.remoteDefinitionUrl = remoteDefinitionBase + "/" + node.getAttribute("id");
		this.remoteDefinitionUrl = remoteDefinitionUrl.replace(" ", "%20");
		this.remoteArchitveUrl = remoteDefinitionUrl + "/" + node.getAttribute("file");
		this.remoteArchiveSize = Long.parseLong(node.getAttribute("size"));
		try {
			// TODO i18n
			URL url = new URL(getRemoteDefinitionUrl() + "/i18n/" + getId() + ".properties");
			remoteProperties.load(url.openStream());
		} catch (MalformedURLException e) {
			throw new IOException(e);
		} catch(IOException ex) {
			throw new IOException(ex);
		}
	}
	
	public Properties getRemoteProperties() {
		return remoteProperties;
	}
	
	public void setLocalArchiveFile(File archive) {
		this.archiveFile = archive;
	}
	public boolean isSystem() {
		return isSystem;
	}
	
	public boolean isLocal() {
		return remoteDefinitionUrl==null;
	}
	
	public String getId() {
		return id;
	}

	public String getLicense() {
		return license;
	}
	
	public String getLicenseUrl() {
		return licenseUrl;
	}
	
	public long getLastModified() {
		return lastModified;
	}
	
	public String getImage() {
		return image;
	}
	
	public String getVendor() {
		return vendor;
	}
	
	public String getUrl() {
		return url;
	}

	public List<String> getDependencies() {
		return dependencies;
	}
	
	void setState(ExtensionState state) {
		this.state = state;
	}
	
	public ExtensionState getState() {
		return state;
	}
	
	public Integer getWeight() {
		return weight;
	}
	
	public String getRemoteDefinitionUrl() {
		return remoteDefinitionUrl;
	}
	
	public String getRemoteArchiveURL() {
		return remoteArchitveUrl;
	}
	
	public Long getRemoteArchiveSize() {
		return remoteArchiveSize;
	}

	@Override
	public int compareTo(ExtensionDefinition ext) {
		return weight.compareTo(ext.getWeight());
	}

	public File getLocalArchiveFile() {
		return archiveFile;
	}

	public long getSize() {
		return size;
	}

	public String getHash() {
		return hash;
	}

	public String getVersion() {
		return version;
	}

}
