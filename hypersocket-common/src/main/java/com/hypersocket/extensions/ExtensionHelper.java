package com.hypersocket.extensions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.json.version.HypersocketVersion;
import com.hypersocket.json.version.Version;
import com.hypersocket.utils.HttpUtilsHolder;
import com.hypersocket.utils.HypersocketUtils;

public class ExtensionHelper {

	static Logger log = LoggerFactory.getLogger(ExtensionHelper.class);

	public static Map<String, ExtensionVersion> resolveExtensions(boolean refresh, String updateUrl, String[] repos,
			String ourVersion, String serial, String product, String customer, ExtensionPlace extensionPlace,
			boolean resolveInstalled, PropertyCallback callback, ExtensionTarget... targets) throws IOException {

		Map<String, ExtensionVersion> extsByName = ExtensionHelper.resolveRemoteDependencies(updateUrl, repos,
				ourVersion, serial, product, customer, callback, targets);

		for (ExtensionVersion ext : extsByName.values()) {
			ext.setState(ExtensionState.NOT_INSTALLED);
		}

		if (!resolveInstalled) {
			return extsByName;
		}

		return processLocalExtensions(extsByName, extensionPlace);

	}

	public static Map<String, ExtensionVersion> processLocalExtensions(
			Map<String, ExtensionVersion> availableExtensions, ExtensionPlace extensionsPlace) throws IOException {

		Map<String, ExtensionVersion> extsByName = new HashMap<String, ExtensionVersion>(availableExtensions);

		try {

			List<URL> urlsList = extensionsPlace.getUrls();
			log.info(String.format("Extension place %s has %d urls", extensionsPlace.getApp(), urlsList.size()));
			for(Map.Entry<String, File> en : extensionsPlace.getBootstrapArchives().entrySet()) {
				log.info(String.format("   %s (%s)", en.getKey(), en.getValue()));
			}
			Iterator<URL> urls = urlsList.iterator();

			while (urls.hasNext()) {
				URL url = urls.next();
				Properties props = new Properties();
				try {
					props.load(url.openStream());
					String extensionId = props.getProperty("extension.id");
					if (log.isInfoEnabled()) {
						log.info("Processing extension definition " + extensionId + " : " + url);
					}

					File currentArchive = extensionsPlace.getBootstrapArchives().get(extensionId);
					if (currentArchive == null) {
						log.warn("No bootstrap archive detected for " + extensionId + " in " + extensionsPlace.getApp());

						if (extsByName.containsKey(extensionId)) {
							ExtensionVersion remote = extsByName.get(extensionId);
							remote.setState(ExtensionState.INSTALLED);
							extsByName.put(extensionId, remote);
							
							if("true".equals(System.getProperty("hypersocket.development", "false"))) {
								Version remoteVersion = new Version(remote.getVersion());
								Version localVersion = new Version(HypersocketVersion.getVersion());
								log.warn("Faking existence of extension for purposes of update testing of " + extensionId + " in " + extensionsPlace.getApp() + ". Using version " + localVersion);

								if (remoteVersion.compareTo(localVersion) >= 0) {

									if (log.isInfoEnabled()) {
										log.info(extensionId + " is installed but an update is available ["
												+ remoteVersion.compareTo(localVersion) + "]");
									}

									remote.setState(ExtensionState.UPDATABLE);
								}
								
//								int[] fakeVersionElements = v.getVersionElements();
//								if(fakeVersionElements.length == 4) {
//									remote.setVersion(fakeVersionElements[0] + "." + fakeVersionElements[1] + "." + fakeVersionElements[2] + "-" + (Math.max(0, fakeVersionElements[3] - 1)));
//									log.warn("Faking existence of extension for purposes of update testing of " + extensionId + " in " + extensionsPlace.getApp() + ". Using version " + remote.getVersion());
//									remote.setState(ExtensionState.UPDATABLE);
//								}
//								else {
//									log.error("Can't fake version. Can't create a smaller version than " + fakeVersion);
//								}
							}
							else
								log.warn("Using installed version of " + remote.getVersion() + " for " + extensionId);
						} else {

							if("true".equals(System.getProperty("hypersocket.development", "false"))) {
								ExtensionVersion local = new ExtensionVersion();
								loadLocalExtension(local, props, currentArchive);
								local.setDescription("You are running in development mode. "
										+ "The extension is not available from the remote repository, but it "
										+ "is already available on your classpath.");
								local.setState(ExtensionState.INSTALLED);
								extsByName.put(extensionId, local);
							}
							else {
								ExtensionVersion local = new ExtensionVersion();
								loadLocalExtension(local, props, currentArchive);
								local.setState(ExtensionState.INSTALLED);
								extsByName.put(extensionId, local);
							}
						}
						continue;
					} else {
						try {
							if (currentArchive.exists()) {
								FileInputStream in = new FileInputStream(currentArchive);
								try {
									props.put("hash", DigestUtils.md5Hex(in));
								} finally {
									IOUtils.closeQuietly(in);
								}
							} else {
								props.put("hash", "??UNKNOWN??");
							}

							ExtensionVersion local = new ExtensionVersion();

							if (extsByName.containsKey(extensionId)) {

								ExtensionVersion remote = extsByName.get(extensionId);
								loadBootstrapExtension(local, remote, props, currentArchive);

								remote.setState(ExtensionState.INSTALLED);

								if (!remote.getHash().equals(local.getHash())) {

									Version remoteVersion = new Version(remote.getVersion());
									Version localVersion = new Version(HypersocketVersion.getVersion());

									if (remoteVersion.compareTo(localVersion) >= 0) {

										if (log.isInfoEnabled()) {
											log.info(extensionId + " is installed but an update is available hash=\""
													+ local.getHash() + "\" modified=\"" + local.getModifiedDate()
													+ "\" size=\"" + currentArchive.length() + "\" ["
													+ remoteVersion.compareTo(localVersion) + "]");
										}

										remote.setState(ExtensionState.UPDATABLE);
									}
								}
							} 
							
							/**
							 * LDP - I think this keeps old extensions alive. Don't include and the update
							 * code should remove the extension.
							 */
//							else {
//								if (log.isInfoEnabled()) {
//									log.info(extensionId + " is installed but not provided by online store");
//								}
//								loadLocalExtension(local, props, currentArchive);
//								extsByName.put(local.getExtensionId(), local);
//							}

						} catch (Throwable e) {
							log.error("Failed to parse local extension definition " + props.getProperty("extension.id"),
									e);
						}
					}

				} catch (IOException e) {
					throw new IOException(
							"Failed to load properties from extension definition " + url.toExternalForm());
				}

			}

			return extsByName;

		} catch (IOException e) {
			throw new IOException("Failed to load extension.def resources", e);
		}
	}

	private static void loadBootstrapExtension(ExtensionVersion ext, ExtensionVersion remote, Properties props,
			File currentArchive) {

		ext.setDescription(remote.getDescription());
		ext.setExtensionId(props.getProperty("extension.id"));
		ext.setExtensionName(remote.getExtensionName());
		ext.setFilename(currentArchive.getName());
		ext.setHash(props.getProperty("hash"));
		ext.setModifiedDate(currentArchive.lastModified());
		ext.setName(remote.getName());
		ext.setRepository(remote.getRepository());
		ext.setRepositoryDescription(remote.getRepositoryDescription());
		ext.setSize(currentArchive.length());
		ext.setState(ExtensionState.INSTALLED);
		ext.setVersion(HypersocketVersion.getVersion());
		ext.setDependsOn(remote.getDependsOn());
		ext.setMandatory(remote.isMandatory());
		ext.setTarget(remote.getTarget());
	}

	private static void loadLocalExtension(ExtensionVersion ext, Properties props, File currentArchive) {

		ext.setDescription("This extension is not available on the remote extension store.");
		ext.setExtensionId(props.getProperty("extension.id"));
		ext.setExtensionName(props.getProperty("extension.id"));
		ext.setFilename(currentArchive == null ? ext.getExtensionId() + ".zip" : currentArchive.getName());
		ext.setHash(props.getProperty("hash"));
		ext.setModifiedDate(currentArchive == null ? System.currentTimeMillis() : currentArchive.lastModified());
		ext.setName(props.getProperty("extension.id"));
		ext.setRepository("local");
		ext.setRepositoryDescription("Local Extensions");
		ext.setSize(currentArchive == null ? 0 : currentArchive.length());
		ext.setState(ExtensionState.INSTALLED);
		ext.setVersion(HypersocketVersion.getVersion());
		ext.setDependsOn(HypersocketUtils.checkNull(props.getProperty("extension.depends")).split(","));
		ext.setMandatory(Boolean.parseBoolean(props.getProperty("extension.system", "false")));
		ext.setTarget("SERVER");
	}

	public static Map<String, ExtensionVersion> resolveRemoteDependencies(String url, String[] repos, String version,
			String serial, String product, String customer, PropertyCallback callback, ExtensionTarget... targets) throws IOException {

		Map<String, ExtensionVersion> extsByName = new HashMap<String, ExtensionVersion>();

		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {

			String additionalRepos = System.getProperty("hypersocket.privateRepos");
			if (additionalRepos != null) {
				if (log.isInfoEnabled()) {
					log.info(String.format("Adding private repos %s", additionalRepos));
				}
				repos = ArrayUtils.addAll(repos, additionalRepos.split(","));
			}
			if(StringUtils.isBlank(additionalRepos)) {
				additionalRepos = System.getProperty("hypersocket.additionalRepos");
				if (additionalRepos != null) {
					if (log.isInfoEnabled()) {
						log.info(String.format("Adding additional repos %s", additionalRepos));
					}
					repos = ArrayUtils.addAll(repos, additionalRepos.split(","));
				}
			}
			String updateUrl = String.format("%s/%s/%s/%s/%s", url, version, HypersocketUtils.csv(repos),
					serial, HypersocketUtils.csv(targets));

			if (log.isInfoEnabled()) {
				log.info("Checking for updates from " + updateUrl);
			}

			Map<String, String> params = new HashMap<String, String>();
			params.put("product", product);
			params.put("customer", customer);
			String output = HttpUtilsHolder.getInstance().doHttpPost(updateUrl, params, true);

			if (log.isDebugEnabled()) {
				log.debug(HypersocketUtils.prettyPrintJson(output));
			}

			ObjectMapper mapper = new ObjectMapper();
			JsonExtensionList exts = mapper.readValue(output, JsonExtensionList.class);
			
			if("true".equalsIgnoreCase(exts.getProperties().get("warn"))) {
				System.setProperty("hypersocket.revocationWarning", "true");
			} else if("true".equalsIgnoreCase(exts.getProperties().get("kill"))) {
				System.exit(0);
			}
			
			if(exts.getProperties().containsKey("message")) {
				System.setProperty("hypersocket.licensingMessage", exts.getProperties().get("message"));
			}
			
			for (ExtensionVersion ext : exts.getResources()) {
				ExtensionTarget t = ExtensionTarget.valueOf(ext.getTarget());
				if (ArrayUtils.contains(targets, t)) {
					extsByName.put(ext.getExtensionId(), ext);
				}
			}
			if(callback!=null) {
				callback.processRemoteProperties(exts.getProperties());
			}

		} catch (Exception ex) {
			throw new IOException("Failed to resolve remote extensions. " + ex.getMessage(), ex);
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
			StringTokenizer t = new StringTokenizer(System.getProperty("hypersocket.bootstrap.archives"), "=;");

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
			StringTokenizer t = new StringTokenizer(System.getProperty("hypersocket.bootstrap.archives"), "=;");

			while (t.hasMoreTokens()) {
				archives.put(t.nextToken(), new File(t.nextToken()));
			}
		}
		return archives;
	}
}
