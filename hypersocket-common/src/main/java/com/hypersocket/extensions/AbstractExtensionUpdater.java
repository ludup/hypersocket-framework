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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.utils.FileUtils;

public abstract class AbstractExtensionUpdater {

	static Logger log = LoggerFactory.getLogger(AbstractExtensionUpdater.class);
	
	public final boolean update()
			throws IOException {
		
		if(log.isInfoEnabled()) {
			log.info("Checking updatable extensions");
		}
		
		try {
			File tmpFolder = Files.createTempDirectory("hypersocket").toFile();
			Map<ExtensionPlace, List<ExtensionDefinition>> extensions = onResolveExtensions();
			Map<ExtensionPlace, List<ExtensionDefinition>> updates = new HashMap<ExtensionPlace, List<ExtensionDefinition>>();
			Map<ExtensionDefinition,File> tmpArchives = new HashMap<ExtensionDefinition,File>();
			
			long totalSize = 0;
			for (Map.Entry<ExtensionPlace, List<ExtensionDefinition>> en : extensions
					.entrySet()) {
				
				List<ExtensionDefinition> toUpdate = new ArrayList<ExtensionDefinition>();
				updates.put(en.getKey(), toUpdate);
				
				for (ExtensionDefinition def : en.getValue()) {
					log.info(String.format("Checking %s - State %s (%d bytes)",
							def.getId(), def.getState(), def.getSize()));
					
					// If this extension place is for cached anciliary apps such as HS client, they are always installed
					if (en.getKey().isDownloadAllExtensions() && def.getState() == ExtensionState.NOT_INSTALLED) {
						def.setState(ExtensionState.UPDATABLE);
					}
					
					if(def.getState() == ExtensionState.UPDATABLE
							|| (installMissing() && def.getState() == ExtensionState.NOT_INSTALLED)) {
						toUpdate.add(def);
						totalSize += def.getSize();
					}
				}
			}
			
			if(totalSize == 0) {
				return false;
			}
			onUpdateStart(totalSize);
			
			long transfered = 0;

			for (Map.Entry<ExtensionPlace, List<ExtensionDefinition>> en : updates
					.entrySet()) {
				File appTmpFolder = new File(tmpFolder, en.getKey().getApp());
				for (ExtensionDefinition def : en.getValue()) {

					onExtensionDownloadStarted(def);


					URL url = new URL(def.getRemoteArchiveUrl());
					
					File archiveTmp = new File(appTmpFolder , 
							FileUtils.lastPathElement(FileUtils.checkEndsWithNoSlash(url.getFile())));
					archiveTmp.getParentFile().mkdirs();

					tmpArchives.put(def, archiveTmp);

					OutputStream out = new FileOutputStream(archiveTmp);
					
					if(log.isInfoEnabled()) {
						log.info(String.format("Downloading %s", url));
					}
					InputStream in = downloadFromUrl(url);

					try {

						byte[] buf = new byte[32768];
						int b;
						long read = 0;
						while ((b = in.read(buf)) > -1) {
							out.write(buf, 0, b);
							onUpdateProgress((long) b, transfered += b);
							read += b;
							if (System
									.getProperty("hypersocket.development.fakeSlowUpdate") != null) {
								Thread.sleep(1000);
							}
						}
						if(read != def.getRemoteArchiveSize()) {
							throw new IOException("Corrupt download for extension "
									+ def.getId() + ". Size is " + read + " bytes, expected " + def.getRemoteArchiveSize() + " bytes");
						}

					} finally {
						FileUtils.closeQuietly(in);
						FileUtils.closeQuietly(out);
					}

					in = new FileInputStream(archiveTmp);
					String generatedMd5 = DigestUtils.md5Hex(in);
					IOUtils.closeQuietly(in);

					if (!generatedMd5.equals(def.getHash())) {
						if (log.isErrorEnabled()) {
							log.error("Install of extension " + def.getId()
									+ " failed. Corrupt download");
						}
						throw new IOException("Corrupt download for extension "
								+ def.getId() + ". Hash is " + generatedMd5 + ", expected " + def.getHash());
					}

					onExtensionDownloaded(def);
				}
				

				
				/**
				 * If we reached here all extensions have updated
				 */
				File archivesDirFile = en.getKey().getDir();
				if(!archivesDirFile.exists() && !archivesDirFile.mkdirs()) {
					throw new IOException(String.format("Archive directory %s does not exist and could not be created.", archivesDirFile.getAbsolutePath()));
				}
				
				
				/**
				 * Rename all the old archives to .bak
				 */
				File[] archives = archivesDirFile.listFiles(new FilenameFilter() {
					
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".zip");
					}
				});
				if(archives!=null) {
					for(File prevArchive : archives) {
						String filename = prevArchive.getName().replace(".zip", ".bak");
						
						File backupFile = new File(archivesDirFile, filename);
						if(!prevArchive.renameTo(backupFile)) {
							throw new IOException("Could not backup previous archive " + prevArchive.getName());
						}
					};
				}
			}
			
			/**
			 * Now copy over the new archives
			 */
			for (Map.Entry<ExtensionPlace, List<ExtensionDefinition>> en : updates
					.entrySet()) {
				for(ExtensionDefinition def : en.getValue()) {
					File archiveTmp = tmpArchives.get(def);
					File archiveFile = new File(en.getKey().getDir(), archiveTmp.getName());
					completeDownload(tmpArchives.get(def), archiveFile, def);
				}
			}
			
			/**
			 * Finally delete the backups
			 */
			for (Map.Entry<ExtensionPlace, List<ExtensionDefinition>> en : updates
					.entrySet()) {
				File[] archives = en.getKey().getDir().listFiles(new FilenameFilter() {
					
					@Override
					public boolean accept(File dir, String name) {
						return name.endsWith(".bak");
					}
				});
				
				if(archives!=null) {
					for(File prevArchive : archives ){
						if(!prevArchive.delete()) {
							log.warn(prevArchive.getName() + " could not be deleted. This should not affect the upgrade but advisable that its removed.");
						}
					};
				}
			}
			
			onUpdateComplete(totalSize);
			
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
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
	
	protected void onExtensionDownloadStarted(ExtensionDefinition def) {
		
	}

	protected void onExtensionDownloaded(ExtensionDefinition def) {
		
	}
	
	protected abstract Map<ExtensionPlace, List<ExtensionDefinition>> onResolveExtensions() throws IOException;
	
	protected abstract void onUpdateStart(long totalBytesExpected);
	
	protected abstract void onUpdateProgress(long sincelastProgress, long totalSoFar);
	
	protected abstract void onUpdateComplete(long totalBytesTransfered);
	
	protected abstract void onUpdateFailure(Throwable e);
	
	protected abstract void onExtensionUpdateComplete(ExtensionDefinition def);
	
	protected boolean installMissing() {
		return false;
	}
	
	private void completeDownload(File archiveTmp, File archiveFile,
			ExtensionDefinition def) throws IOException {

		InputStream in = new FileInputStream(archiveTmp);
		if(!archiveFile.getParentFile().exists()) {
			archiveFile.getParentFile().mkdirs();
		}
		if(!archiveFile.exists()) {
			archiveFile.createNewFile();
		}
		OutputStream out = new FileOutputStream(archiveFile);
		
		try {
			IOUtils.copy(in, out);
		} finally {
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(out);
		}
		
		archiveFile.setLastModified(def.getLastModified()*1000);
		archiveTmp.delete();

		if(log.isInfoEnabled()) {
			log.info("Install of extension " + def.getId() + " completed ok");
		}
		
		onExtensionUpdateComplete(def);
	
	}

}
