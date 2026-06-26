package com.hypersocket.profile.export;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.io.ZipExportService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;

@Service
public class ProfileExportServiceImpl implements ProfileExportService {
	
	private static final Logger log = LoggerFactory.getLogger(ProfileExportServiceImpl.class);
	
	private final Set<ProfileExporter> exporterRegistry = ConcurrentHashMap.newKeySet();
	
	@Autowired
	private ZipExportService zipExportService;
	
	@Override
	public void registerExporter(ProfileExporter exporter) {
        if (exporter == null) return;
        
        // Atomic add. Returns true if added, false if already present.
        boolean isNew = exporterRegistry.add(exporter);
        
        if (isNew) {
            log.info("Registered new dynamic exporter: {}", exporter.getClass().getName());
        } else {
            log.debug("Exporter already registered: {}", exporter.getClass().getName());
        }
    }

	@Override
    public void unregisterExporter(ProfileExporter exporter) {
    	exporterRegistry.remove(exporter);
    }
    
	@Override
    public Set<ProfileExporter> allProfileExporters() {
    	return Collections.unmodifiableSet(exporterRegistry);
    }
    
    @Override
    public void zipAllProfiles(OutputStream outputStream, Realm realm) throws IOException, AccessDeniedException {
		List<ZipExportService.FileSpec> files = new ArrayList<>();
		
		var profileExporters = allProfileExporters();
		for (var profileExporter : profileExporters) {
			var fileName = profileExporter.fileName();
			log.info("Processing file {} for profile export.", fileName);
			files.add(new ZipExportService.FileSpec(profileExporter.fileName(), profileExporter.generator(), realm));
		}
			
        zipExportService.streamZipFile(files, outputStream);
    }

}
