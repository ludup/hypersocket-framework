package com.hypersocket.migration.file;

import com.hypersocket.migration.repository.MigrationRepository;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.upload.FileUpload;
import com.hypersocket.upload.FileUploadService;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class FileUploadExporter {

    @Autowired
    MigrationRepository migrationRepository;

    @Autowired
    RealmService realmService;

    @Autowired
    FileUploadService fileUploadService;

    public void start(Realm realm, ZipOutputStream zipOutputStream) throws IOException, ResourceNotFoundException {
        List<FileUpload> fileUploadList = migrationRepository.findAllResourceInRealmOfType(FileUpload.class, realm);
        if(fileUploadList != null) {
            for (FileUpload fileUpload : fileUploadList) {
                File file = fileUploadService.getFile(fileUpload.getName());
                if (Boolean.getBoolean("hypersocket.development") && !file.exists()) {
                    continue;
                }
                ZipEntry anEntry = new ZipEntry("uploadedFiles" + File.separatorChar + fileUpload.getName());
                zipOutputStream.putNextEntry(anEntry);
                InputStream inputStream = FileUtils.openInputStream(file);
                IOUtils.copy(inputStream, zipOutputStream);
                IOUtils.closeQuietly(inputStream);
                zipOutputStream.closeEntry();
                zipOutputStream.flush();
            }
        }
    }
}
