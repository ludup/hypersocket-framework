package com.hypersocket.extensions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.HypersocketVersion;
import com.hypersocket.utils.FileUtils;
import com.hypersocket.utils.HypersocketUtils;

public abstract class AbstractExtensionUpdater {

	static Logger log = LoggerFactory.getLogger(AbstractExtensionUpdater.class);
	private long transfered;
	private long totalSize;

	public long getTransfered() {
		return transfered;
	}

	public long getTotalSize() {
		return totalSize;
	}
	
	public abstract ExtensionPlace getExtensionPlace();

	public abstract String getUpdateTarget();
	
	public abstract String getVersion();
	
	public final boolean update() throws IOException {

		if (log.isInfoEnabled()) {
			log.info("Checking updatable extensions");
		}

		int totalUpdates = 0;
		try {
			File tmpFolder = Files.createTempDirectory("hypersocket").toFile();
			File backupFolder = new File(HypersocketUtils.getConfigDir().getParent(), "backups");
			backupFolder = new File(backupFolder, HypersocketVersion.getVersion());
			FileUtils.deleteFolder(backupFolder);
			if (!backupFolder.exists()) {
				backupFolder.mkdirs();
			}
			final Map<String, ExtensionVersion> allExtensions = onResolveExtensions(getVersion());
			List<ExtensionVersion> updates = new ArrayList<ExtensionVersion>();
			
			for(ExtensionVersion v : allExtensions.values()) {
				switch(v.getState()) {
				case UPDATABLE:
					if(getUpdateTarget().equals(v.getTarget())) {
						updates.add(v);
						for(String depend : v.getDependsOn()) {
							if(StringUtils.isNotBlank(depend)) {
								ExtensionVersion dep = allExtensions.get(depend);
								if(dep.getState()==ExtensionState.UPDATABLE || dep.getState()==ExtensionState.NOT_INSTALLED) {
									updates.add(dep);
								}
							}
						}
					}
					break;
				case NOT_INSTALLED:
					if(getUpdateTarget().equals(v.getTarget())) {
						if(v.isMandatory()) {
							updates.add(v);
							for(String depend : v.getDependsOn()) {
								if(StringUtils.isNotBlank(depend)) {
									ExtensionVersion dep = allExtensions.get(depend);
									if(dep.getState()==ExtensionState.UPDATABLE || dep.getState()==ExtensionState.NOT_INSTALLED) {
										updates.add(dep);
									}
								}
							}
						}
					}
					break;
				default:
					break;
				}
			}
			
			Map<ExtensionVersion, File> tmpArchives = new HashMap<ExtensionVersion, File>();

			totalSize = 0;

			for (ExtensionVersion def : updates) {
				log.info(String.format("Checking %s - State %s (%d bytes)", 
						def.getId(), 
						def.getState(),
						def.getSize()));

					totalUpdates++;
					totalSize += def.getSize();
			}

			if (totalUpdates == 0) {
				return false;
			}
			onUpdateStart(totalSize);

			transfered = 0;

			List<File> toRemove = new ArrayList<>();

			for (ExtensionVersion def : updates) {

				onExtensionDownloadStarted(def);

				URL url = new URL(def.getUrl());

				File archiveTmp = new File(tmpFolder,
						FileUtils.lastPathElement(FileUtils.checkEndsWithNoSlash(url.getFile())));
				archiveTmp.getParentFile().mkdirs();

				tmpArchives.put(def, archiveTmp);

				OutputStream out = new FileOutputStream(archiveTmp);

				if (log.isInfoEnabled()) {
					log.info(String.format("Downloading %s", url));
				}
				try {
					InputStream in = downloadFromUrl(url);

					try {

						byte[] buf = new byte[32768];
						int b;
						long read = 0;
						while ((b = in.read(buf)) > -1) {
							out.write(buf, 0, b);
							onUpdateProgress((long) b, transfered += b, totalSize);
							read += b;
							if (System.getProperty("hypersocket.development.fakeSlowUpdate") != null) {
								Thread.sleep(1000);
							}
						}

						if (read != def.getSize()) {
							throw new IOException("Corrupt download for extension " + def.getId() + ". Size is "
									+ read + " bytes, expected " + def.getSize() + " bytes");
						}

					} finally {
						FileUtils.closeQuietly(in);
					}
				} finally {
					FileUtils.closeQuietly(out);
				}

				InputStream in = new FileInputStream(archiveTmp);
				try {
					String generatedMd5 = DigestUtils.md5Hex(in);
					if (def.getHash().length() > 0 && !generatedMd5.equals(def.getHash())) {
						if (log.isErrorEnabled()) {
							log.error("Install of extension " + def.getId() + " failed. Corrupt download");
						}
						throw new IOException("Corrupt download for extension " + def.getId() + ". Hash is "
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
							if (bn.matches(d.getId() + "-(\\d+\\.?)+.*")) {
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

			if (archives != null) {
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
					log.info("Will not remove " + archiveFile);
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
				log.info("Moving " + f + " to backup");
				try {
					Files.move(f.toPath(), new File(backupFolder, f.getName()).toPath());
				} catch (IOException ex) {
					log.error(String.format("Failed to move %s to backup folder", f.getName()));
					if (!f.delete()) {
						log.warn(f.getName()
								+ " could not be deleted. The file has been marked to delete upon JVM exit, but this may or may not work. This should not affect the upgrade but advisable that its removed.");
						f.deleteOnExit();
					}
				}

			}

			onUpdateComplete(totalSize);

		} catch (Throwable e) {
			if (log.isErrorEnabled()) {
				log.error("Updating extensions failed", e);
			}

			onUpdateFailure(e);

			throw new IOException(e);
		}

		return true;
	}

	protected InputStream downloadFromUrl(URL url) throws IOException {
		InputStream in = url.openStream();
		return in;
	}

	protected void onExtensionDownloadStarted(ExtensionVersion def) {

	}

	protected void onExtensionDownloaded(ExtensionVersion def) {

	}

	protected abstract Map<String, ExtensionVersion> onResolveExtensions(String version) throws IOException;

	protected abstract void onUpdateStart(long totalBytesExpected);

	protected abstract void onUpdateProgress(long sincelastProgress, long totalSoFar, long totalBytesException);

	protected abstract void onUpdateComplete(long totalBytesTransfered);

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

			archiveFile.setLastModified(def.getModifiedDate() * 1000);
			log.info("Deleting temporary file " + archiveTmp);
			archiveTmp.delete();

			if (log.isInfoEnabled()) {
				log.info("Install of extension " + def.getId() + " to " + archiveFile + " completed ok");
			}
		} finally {
			IOUtils.closeQuietly(in);
		}

		onExtensionUpdateComplete(def);

	}

}
