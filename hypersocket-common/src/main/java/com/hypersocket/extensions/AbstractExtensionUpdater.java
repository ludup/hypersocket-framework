package com.hypersocket.extensions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.HypersocketVersion;
import com.hypersocket.utils.FileUtils;
import com.hypersocket.utils.HttpUtilsHolder;
import com.hypersocket.utils.HypersocketUtils;

public abstract class AbstractExtensionUpdater {

	private static final int MAX_BACKUPS = Integer.parseInt(System.getProperty("hypersocket.maxExtensionBackups", "10"));

	static Logger log = LoggerFactory.getLogger(AbstractExtensionUpdater.class);
	
	private long transfered;
	private long totalSize;
	private int totalUpdates;
	
	private Map<ExtensionVersion, File> tmpArchives;
	private Set<ExtensionVersion> updates;
	private Map<String, ExtensionVersion> allExtensions;
	
	private boolean checked;
	private File backupFolder;

	private File backupRootFolder;
	
	public long getTransfered() {
		return transfered;
	}

	public long getTotalSize() {
		return totalSize;
	}
	
	public abstract ExtensionPlace getExtensionPlace();

	public abstract ExtensionTarget[] getUpdateTargets();
	
	public abstract String getVersion();
	
	public abstract boolean hasLocalRepository(String version);
	
	public abstract File getLocalRepositoryFile();
	
	public abstract Set<String> getNewFeatures();
	
	protected abstract boolean getInstallMandatoryExtensions();
	
	public boolean checkForUpdates() throws IOException {
	
		if (log.isInfoEnabled()) {
			log.info("Checking updatable extensions");
		}
		
		backupRootFolder = new File(HypersocketUtils.getConfigDir().getParent(), "backups");
		backupFolder = new File(backupRootFolder, HypersocketVersion.getVersion());
		FileUtils.deleteFolder(backupFolder);
		if (!backupFolder.exists()) {
			backupFolder.mkdirs();
		}
		allExtensions = onResolveExtensions(getVersion());
		updates = new HashSet<ExtensionVersion>();
		
		
		for(ExtensionVersion v : allExtensions.values()) {
			switch(v.getState()) {
			case UPDATABLE:
				
				if(ArrayUtils.contains(getUpdateTargets(),ExtensionTarget.valueOf(v.getTarget()))) {
					updates.add(v);
					for(String depend : v.getDependsOn()) {
						if(StringUtils.isNotBlank(depend)) {
							ExtensionVersion dep = allExtensions.get(depend);
							updates.add(dep);
						}
					}
				}
				
				break;
			case NOT_INSTALLED:
				
				if(ArrayUtils.contains(getUpdateTargets(), ExtensionTarget.valueOf(v.getTarget()))) {
					if(log.isDebugEnabled()) {
						log.debug(String.format("Checking install state for %s %s", v.getExtensionId(), v.getFeatureGroup()));
					}
					if(getInstallMandatoryExtensions() && v.isMandatory() || getNewFeatures().contains(v.getFeatureGroup())) {
						updates.add(v);
						for(String depend : v.getDependsOn()) {
							if(StringUtils.isNotBlank(depend)) {
								ExtensionVersion dep = allExtensions.get(depend);
								updates.add(dep);
							}
						}
					}
				}
				
				break;
			default:
				break;
			}
		}
		
		tmpArchives = new HashMap<ExtensionVersion, File>();

		totalSize = 0;

		for (ExtensionVersion def : updates) {
			log.info(String.format("Checking %s %s - State %s (%d bytes)", 
					def.getVersion(),
					def.getExtensionId(), 
					def.getState(),
					def.getSize()));

				totalUpdates++;
				totalSize += def.getSize();
		}
		
		checked = true;
		return totalSize > 0;
	}
	
	public final boolean update() throws IOException {

		try {

			if(!checked) {
				if(!checkForUpdates()) {
					if(log.isInfoEnabled()) {
						log.info("No updates");
					}
					return false;
				}
			}
			
			File tmpFolder = Files.createTempDirectory("hypersocket").toFile();
			onUpdateStart(totalSize);

			transfered = 0;

			List<File> toRemove = new ArrayList<>();

			for (ExtensionVersion def : updates) {

				onExtensionDownloadStarted(def);
				InputStream in = null;
				String filename = FileUtils.lastPathElement(def.getFilename());
				if(hasLocalRepository(getVersion())) {
					File file = new File(getLocalRepositoryFile(), def.getFilename());
					in = new FileInputStream(file);
					if (log.isInfoEnabled()) {
						log.info(String.format("Copying %s", file.getAbsolutePath()));
					}
				} else {
					URL url = resolveExtensionURL(def);
					if (log.isInfoEnabled()) {
						log.info(String.format("Downloading %s", url));
					}
					in = downloadFromUrl(url);
				}

				File archiveTmp = new File(tmpFolder,filename);
				archiveTmp.getParentFile().mkdirs();
				tmpArchives.put(def, archiveTmp);
				
				try {
					
					OutputStream out = new FileOutputStream(archiveTmp);
					
					try {

						byte[] buf = new byte[1024 * 1024];
						int b;
						long read = 0;
						String delayStr = System.getProperty("hypersocket.development.fakeSlowUpdate");
						long delay = 0;
						if(delayStr != null) {
							try {
								delay = Long.parseLong(delayStr);
							}
							catch(NumberFormatException nfe) {
								delay = 50;
							}
						}
						while ((b = in.read(buf)) > -1) {
							out.write(buf, 0, b);
							onUpdateProgress((long) b, transfered += b, totalSize);
							read += b;
							if (delay > 0) {
								Thread.sleep(delay);
							}
						}

						if (read != def.getSize()) {
							throw new IOException("Corrupt download for extension " + def.getExtensionId() + ". Size is "
									+ read + " bytes, expected " + def.getSize() + " bytes");
						}

					} finally {
						
						FileUtils.closeQuietly(out);
					}
				} finally {
					FileUtils.closeQuietly(in);
				}

				in = new FileInputStream(archiveTmp);
				try {
					String generatedMd5 = DigestUtils.md5Hex(in);
					if (def.getHash().length() > 0 && !generatedMd5.equals(def.getHash())) {
						if (log.isErrorEnabled()) {
							log.error("Install of extension " + def.getExtensionId() + " failed. Corrupt download");
						}
						throw new IOException("Corrupt download for extension " + def.getExtensionId() + ". Hash is "
								+ generatedMd5 + ", expected " + def.getHash());
					}
				} finally {
					IOUtils.closeQuietly(in);
				}

				onExtensionDownloaded(def);
			}

			File archivesDirFile = getExtensionPlace().getDir();
			if (!archivesDirFile.exists() && !archivesDirFile.mkdirs()) {
				throw new IOException(
						String.format("Archive directory %s does not exist and could not be created.",
								archivesDirFile.getAbsolutePath()));
			}
			
			File[] archives = archivesDirFile.listFiles(new FilenameFilter() {

				@Override
				public boolean accept(File dir, String name) {
					if (name.endsWith(".zip")) {
						String bn = FilenameUtils.getBaseName(name);
						boolean found = false;
						for (ExtensionVersion d : allExtensions.values()) {
							if (bn.matches(d.getExtensionId() + "-(\\d+\\.?)+.*")) {
								found = true;
								if (d.getState() == ExtensionState.UPDATABLE) {
									// The extension was updated or
									// installed, we can move any
									// previous version
									log.info(name + " was updated, will removed");
									return true;
								}

								break;
							}
						}

						if (!found) {
							// The extension no longer exists, so we can
							// move it out
							log.info(name + " is no longer used by any extension, will remove");
							return true;
						}
					}
					log.info(name + " was not updated or obsoleted, will not remove");

					return false;
				}
			});

			if (archives != null && archives.length > 0) {
				// Add to the list of files to be removed on success
				List<File> toRemoveList = Arrays.asList(archives);
				toRemove.addAll(toRemoveList);
				log.info("Considering " + toRemoveList + " for removal");
			}

			/**
			 * Now copy over the new archives
			 */
			List<File> newFiles = new ArrayList<>();
			try {

				for (ExtensionVersion def : updates) {
					File archiveTmp = tmpArchives.get(def);
					File archiveFile = new File(getExtensionPlace().getDir(), archiveTmp.getName());
					if(!archiveFile.getParentFile().exists() && !archiveFile.getParentFile().mkdirs()) {
						throw new IOException(
								String.format("Archive directory %s does not exist and could not be created.",
										archiveFile.getParentFile().getAbsolutePath()));
					}
					newFiles.add(archiveFile);
					completeDownload(tmpArchives.get(def), archiveFile, def);

					/*
					 * Make sure any new extensions with identical versions
					 * are not deleted
					 */
					toRemove.remove(archiveFile);
					log.info("Will remove or backup " + archiveFile);
				}
				
			} catch (IOException ioe) {
				// Remove all of the new files created so the old ones continue
				// to be used
				log.warn("An error occured downloading extensions, reverting to preview extensions");
				for (File f : newFiles) {
					if (f.exists() && !f.delete()) {
						log.warn(f.getName()
								+ " could not be deleted. The file has been marked to delete up JVM exit, but this may or may not work. This should not affect the upgrade but advisable that its removed.");
						f.deleteOnExit();
					}
				}
				throw ioe;
			}

			/**
			 * Finally delete the current files
			 */
			for (File f : toRemove) {
				Path target = new File(backupFolder, f.getName()).toPath();
				Path source = f.toPath();
				log.info(String.format("Moving %s to %s", source, target));
				try {
					Files.move(source, target);
					log.info(String.format("Moved %s to %s, checking it is there", source, target));
					if(Files.exists(target)) {
						log.info(String.format("Backup of %s to %s all good", source, target));
					}
					else {
						log.warn(String.format("Backup of %s to %s does not exist!", source, target));
						Files.copy(source, target);
						if(Files.exists(target)) {
							log.info(String.format("Copy worked, backup of %s to %s all good", source, target));
						}
						else {
							log.error(String.format("Failed to either copy or move %s to %s, given up, extension backup will be incomplete.", source ,target));
						}
						throw new IOException("Copy was used.");
					}
				} catch (IOException ex) {
					log.error(String.format("Failed to move %s to backup folder", f.getName()));
					if (!f.delete()) {
						log.warn(f.getName()
								+ " could not be deleted. The file has been marked to delete upon JVM exit, but this may or may not work. This should not affect the upgrade but advisable that its removed.");
						f.deleteOnExit();
					}
				}

			}
			
			if(backupRootFolder.exists()) {
				/* Trim to MAX_BACKUPS based on modification date of folder */
				List<File> dirs = new ArrayList<>(Arrays.asList(backupRootFolder.listFiles((dir, name) -> dir.isDirectory())));
				if (dirs.size() > MAX_BACKUPS) {
					log.error(String.format("There are now more backup folders (%d) than the maximum of %d, trimming", dirs.size(), MAX_BACKUPS));
					Collections.sort(dirs, (o1, o2) -> Long.valueOf(o1.lastModified()).compareTo(o2.lastModified()));
					for (int i = 0; i < dirs.size() - MAX_BACKUPS; i++) {
						log.error(String.format("Trimming backups Failed to move %s to backup folder", dirs.get(i)));
						FileUtils.deleteFolder(dirs.get(i));
					}
				}
			}

			onUpdateComplete(totalSize, totalUpdates);

		} catch (Exception e) {
			onUpdateFailure(e);

			throw new IOException(e.getMessage(), e);
		}

		return totalUpdates > 0;
	}

	protected InputStream downloadFromUrl(URL url) throws IOException {
		return HttpUtilsHolder.getInstance().doHttpGet(url.toExternalForm(), true);
	}

	protected void onExtensionDownloadStarted(ExtensionVersion def) {

	}

	protected void onExtensionDownloaded(ExtensionVersion def) {

	}

	protected abstract Map<String, ExtensionVersion> onResolveExtensions(String version) throws IOException;

	protected abstract void onUpdateStart(long totalBytesExpected);

	protected abstract void onUpdateProgress(long sincelastProgress, long totalSoFar, long totalBytesException);

	protected abstract void onUpdateComplete(long totalBytesTransfered, int totalUpdates);

	protected abstract void onUpdateFailure(Throwable e);

	protected abstract void onExtensionUpdateComplete(ExtensionVersion def);

	private void completeDownload(File archiveTmp, File archiveFile, ExtensionVersion def) throws IOException {

		InputStream in = new FileInputStream(archiveTmp);
		try {
			if (!archiveFile.getParentFile().exists()) {
				archiveFile.getParentFile().mkdirs();
			}
			if (!archiveFile.exists()) {
				archiveFile.createNewFile();
			}
			OutputStream out = new FileOutputStream(archiveFile);

			try {
				IOUtils.copy(in, out);
			} finally {
				IOUtils.closeQuietly(out);
			}

			archiveFile.setLastModified(def.getModifiedDate());
			log.info("Deleting temporary file " + archiveTmp);
			archiveTmp.delete();

			if (log.isInfoEnabled()) {
				log.info("Install of extension " + def.getExtensionId() + " to " + archiveFile + " completed ok");
			}
		} finally {
			IOUtils.closeQuietly(in);
		}

		onExtensionUpdateComplete(def);

	}
	
	public static URL resolveExtensionURL(ExtensionVersion def) {
		URL url;
		try {
			url = new URL(def.getUrl());
		}
		catch(MalformedURLException murle) {
			/* Relative URL? */
			try {
				url = new URI(getExtensionStoreRoot()).resolve(def.getUrl()).toURL();
			} catch (MalformedURLException |  URISyntaxException e) {
				throw new IllegalArgumentException("Invalid extension URL " + def.getUrl(), e);
			}
		}
		return url;
	}

	public static String getExtensionStoreRoot() {
		String url = System.getProperty("hypersocket.archivesURL",
				"https://updates2.hypersocket.com/hypersocket/");
		return url.endsWith("/") ? url : url + "/";
	}

}
