package com.hypersocket.extensions;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hypersocket.HypersocketVersion;
import com.hypersocket.Version;

public class ExtensionHelper {

	static Logger log = LoggerFactory.getLogger(ExtensionHelper.class);

	public static Map<String, ExtensionDefinition> resolveExtensions(
			boolean refresh, String updateUrl, String appId, String repo)
			throws IOException {

		Map<String, ExtensionDefinition> extsByName = new HashMap<String, ExtensionDefinition>();
		try {
			extsByName.putAll(ExtensionHelper.resolveRemoteDependencies(
					updateUrl, appId, repo));
		} catch (Exception e) {
			log.error("Failed to get remote dependencies", e);
		}

		Map<String, File> bootstrapArchives = ExtensionHelper
				.getBootstrapArchives();
		try {

			Enumeration<URL> urls = ExtensionDefinition.class.getClassLoader()
					.getResources("extension.def");

			if (!urls.hasMoreElements()) {

				if (log.isInfoEnabled()) {
					log.info("No extension.def resources could be found on the classpath");
				}
				urls = ExtensionDefinition.class.getClassLoader().getResources(
						"/extension.def");
			}

			while (urls.hasMoreElements()) {
				URL url = urls.nextElement();
				Properties props = new Properties();
				try {
					props.load(url.openStream());
					String extensionId = props.getProperty("extension.id");
					if (log.isInfoEnabled()) {
						log.info("Processing extension definition "
								+ extensionId);
					}

					File currentArchive = bootstrapArchives.get(extensionId);
					if (currentArchive == null) {
						log.warn("No bootstrap archive detected for "
								+ extensionId);
						if (Boolean.getBoolean("hypersocket.development")) {
							ExtensionDefinition ext = new ExtensionDefinition(
									props, System.currentTimeMillis(),
									currentArchive,
									HypersocketVersion.getVersion());
							ext.setState(ExtensionState.INSTALLED);
							extsByName.put(ext.getId(), ext);
						}
						continue;
					} else {
						try {
							if (currentArchive.exists()) {
								FileInputStream in = new FileInputStream(
										currentArchive);
								try {
									props.put("hash", DigestUtils.md5Hex(in));
								} finally {
									IOUtils.closeQuietly(in);
								}
							} else {
								props.put("hash", "??UNKNOWN??");
							}
							ExtensionDefinition ext = new ExtensionDefinition(
									props, currentArchive.lastModified(),
									currentArchive, HypersocketVersion.getVersion());
							if (extsByName.containsKey(ext.getId())) {
								ExtensionDefinition remote = extsByName.get(ext
										.getId());

								if (remote.getHash().equals(ext.getHash())) {
									if (log.isInfoEnabled()) {
										log.info(extensionId
												+ " is installed ["
												+ ext.getHash() + "]");
									}
									ext.setState(ExtensionState.INSTALLED);
									extsByName.put(ext.getId(), ext);
								} else {
									
									Version remoteVersion = new Version(remote.getVersion());
									Version localVersion = new Version(HypersocketVersion.getVersion());
									
									if(remoteVersion.compareTo(localVersion) >= 0) {
										
										if (log.isInfoEnabled()) {
											log.info(extensionId
													+ " is installed but an update is available hash=\""
													+ ext.getHash()
													+ "\" modified=\""
													+ ext.getLastModified()
													+ "\" size=\""
													+ currentArchive.length()
													+ "\" [" + remoteVersion.compareTo(localVersion) + "]");
										}
										
										remote.setState(ExtensionState.UPDATABLE);
									} else {
									
										if(log.isInfoEnabled()) {
											log.info("Although the current repository version of " 
													+ extensionId + " differs its version " 
													+ ext.getVersion() + " is earlier than the current version " 
													+ HypersocketVersion.getVersion());;
										}
										remote.setState(ExtensionState.INSTALLED);
									}
								}
							} else {
								if (log.isInfoEnabled()) {
									log.info(extensionId
											+ " is installed but not provided by online store");
								}
								ext.setState(ExtensionState.INSTALLED);
								extsByName.put(ext.getId(), ext);
							}

						} catch (Throwable e) {
							log.error(
									"Failed to parse local extension definition "
											+ props.getProperty("extension.id"),
									e);
						}
					}

				} catch (IOException e) {
					throw new IOException(
							"Failed to load properties from extension definition "
									+ url.toExternalForm());
				}

			}

			return extsByName;

		} catch (IOException e) {
			throw new IOException("Failed to load extension.def resources", e);
		}

	}

	public static Map<String, ExtensionDefinition> resolveRemoteDependencies(
			String url, String appId, String repo) throws IOException {

		Map<String, ExtensionDefinition> extsByName = new HashMap<String, ExtensionDefinition>();

		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {

			String updateUrl = url + "/" + appId + "/" + repo + "/"
					+ HypersocketVersion.getVersion();

			if (log.isInfoEnabled()) {
				log.info("Checking for updates from " + updateUrl);
			}

			HttpPost postMethod = new HttpPost(
					updateUrl
							+ "/"
							+ (Boolean.getBoolean("hypersocket.development") ? "DEV_SERIAL"
									: HypersocketVersion.getSerial()));

			List<NameValuePair> nvps = new ArrayList<NameValuePair>();

			StringBuffer buf = new StringBuffer();
			for (String ext : getInstalledExtensions()) {
				if (buf.length() > 0) {
					buf.append(",");
				}
				buf.append(ext);
			}

			nvps.add(new BasicNameValuePair("exts", buf.toString()));
			postMethod.setEntity(new UrlEncodedFormEntity(nvps));
			HttpResponse response = httpclient.execute(postMethod);

			DocumentBuilderFactory xmlFactory = DocumentBuilderFactory
					.newInstance();
			DocumentBuilder xmlBuilder = xmlFactory.newDocumentBuilder();

			String output = IOUtils.toString(response.getEntity().getContent());

			if (log.isInfoEnabled()) {
				log.info(output);
			}

			Document doc = xmlBuilder.parse(new ByteArrayInputStream(output
					.getBytes("UTF-8")));

			String appUrl = doc.getDocumentElement().getAttribute("url");
			String repoVersion = doc.getDocumentElement().getAttribute("version");
			NodeList list = doc.getElementsByTagName("archive");
			Map<String, File> localArchives = getBootstrapArchives();
			for (int i = 0; i < list.getLength(); i++) {
				Node node = list.item(i);
				try {
					ExtensionDefinition ext = new ExtensionDefinition(
							(Element) node, appUrl, repoVersion);
					ext.setLocalArchiveFile(localArchives.get(ext.getId()));
					extsByName.put(ext.getId(), ext);
				} catch (Throwable e) {
					log.error("Failed to parse remote extension definition", e);
				}
			}

		} catch (Exception ex) {
			throw new IOException("Failed to resolve remote extensions", ex);
		} finally {
			try {
				httpclient.close();
			} catch (IOException e) {
			}
		}

		return extsByName;
	}

	public static List<String> getInstalledExtensions() {
		List<String> exts = new ArrayList<String>();

		if (System.getProperty("hypersocket.bootstrap.archives") != null) {
			StringTokenizer t = new StringTokenizer(
					System.getProperty("hypersocket.bootstrap.archives"), "=;");

			while (t.hasMoreTokens()) {
				exts.add(t.nextToken().trim());
				if (t.hasMoreTokens()) {
					t.nextToken();
				}
			}
		}
		return exts;
	}

	public static Map<String, File> getBootstrapArchives() {

		Map<String, File> archives = new HashMap<String, File>();

		if (System.getProperty("hypersocket.bootstrap.archives") != null) {
			StringTokenizer t = new StringTokenizer(
					System.getProperty("hypersocket.bootstrap.archives"), "=;");

			while (t.hasMoreTokens()) {
				archives.put(t.nextToken(), new File(t.nextToken()));
			}
		}
		return archives;
	}
}
