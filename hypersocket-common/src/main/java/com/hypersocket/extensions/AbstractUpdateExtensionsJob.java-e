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
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hypersocket.utils.FileUtils;

public abstract class AbstractUpdateExtensionsJob implements Job {

	static Logger log = LoggerFactory.getLogger(AbstractUpdateExtensionsJob.class);
	

	@Override
	public void execute(JobExecutionContext context)
			throws JobExecutionException {

		onInitUpdate(context);
		
		if(log.isInfoEnabled()) {
			log.info("Checking updatable extensions");
		}
		
		String archivesDir = System.getProperty("hypersocket.bootstrap.archivesDir");
		if(archivesDir==null) {
			throw new JobExecutionException("hypersocket.bootstrap.archivesDir system property has not been set!");
		}
		
		try {
			File tmpFolder = Files.createTempDirectory("hypersocket").toFile();
			List<ExtensionDefinition> extensions = onResolveExtensions();
			List<ExtensionDefinition> updates = new ArrayList<ExtensionDefinition>();
			Map<ExtensionDefinition,File> tmpArchives = new HashMap<ExtensionDefinition,File>();
			
			long totalSize = 0;
			for (ExtensionDefinition def : extensions) {
				if (def.getState() == ExtensionState.UPDATABLE || (installMissing() && def.getState() == ExtensionState.NOT_INSTALLED)) {
					updates.add(def);
					totalSize += def.getSize();
				}
			}
			
			if(totalSize == 0) {
				throw new JobExecutionException("Nothing to update");
			}
			onUpdateStart(totalSize);
			
			long transfered = 0;
			
			for(ExtensionDefinition def : updates) {

					onExtensionDownloadStarted(def);
					
					URL url = new URL(def.getRemoteArchiveURL());
					
					File archiveTmp = new File(tmpFolder, FileUtils.lastPathElement(url.getFile()));
					archiveTmp.getParentFile().mkdirs();
		
					tmpArchives.put(def, archiveTmp);
		
					OutputStream out = new FileOutputStream(archiveTmp);
					InputStream in = url.openStream();
					
					try {
					
						byte[] buf = new byte[32768];
						int b;
						while((b = in.read(buf)) > -1) {
							out.write(buf, 0, b);
							onUpdateProgress((long)b, transfered+=b);
						}
						
					} finally {
						FileUtils.closeQuietly(in);
						FileUtils.closeQuietly(out);
					}
					
					in  = new FileInputStream(archiveTmp);
					String generatedMd5 = DigestUtils.md5Hex(in);
					IOUtils.closeQuietly(in);
					
					if(!generatedMd5.equals(def.getHash())) {
						if(log.isErrorEnabled()) {
							log.error("Install of extension " + def.getId() + " failed. Corrupt download");
						}
						throw new JobExecutionException("Corrupt download for extension " + def.getId());
					}
					
					onExtensionDownloaded(def);
			}
		
			/**
			 * If we reached here all extensions have updated
			 */
			File archivesDirFile = new File(archivesDir);
			
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
						throw new JobExecutionException("Could not backup previous archive " + prevArchive.getName());
					}
				};
			}
			
			/**
			 * Now copy over the new archives
			 */
			for(ExtensionDefinition def : updates) {
				
				File archiveTmp = tmpArchives.get(def);
				File archiveFile = new File(archivesDirFile, archiveTmp.getName());
				completeDownload(tmpArchives.get(def), archiveFile, def);
			}
			
			/**
			 * Finally delete the backups
			 */
			archives = archivesDirFile.listFiles(new FilenameFilter() {
				
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
			
			onUpdateComplete(totalSize);
			
		} catch(Throwable e) {
			if(log.isErrorEnabled()) {
				log.error("Updating extensions failed", e);
			}
			
			onUpdateFailure(e);
			
			throw new JobExecutionException(e);
		}
	}
	
	protected void onExtensionDownloadStarted(ExtensionDefinition def) {
		
	}

	protected void onExtensionDownloaded(ExtensionDefinition def) {
		
	}

	protected abstract void onInitUpdate(JobExecutionContext context) throws JobExecutionException;
	
	protected abstract List<ExtensionDefinition> onResolveExtensions() throws IOException;
	
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
