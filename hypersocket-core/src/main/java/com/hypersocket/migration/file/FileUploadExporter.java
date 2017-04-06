package com.hypersocket.migration.file;

import com.hypersocket.migration.repository.MigrationRepository;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.upload.FileUpload;
import com.hypersocket.upload.FileUploadService;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;
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

    public void start(Realm realm, ZipOutputStream zipOutputStream) throws IOException {
        if(realm == null) {
            realm = realmService.getCurrentRealm();
        }

        List<FileUpload> fileUploadList = migrationRepository.findAllResourceInRealmOfType(FileUpload.class, realm);
        if(fileUploadList != null) {
            for (FileUpload fileUpload : fileUploadList) {
                try {
                    InputStream inputStream = fileUploadService.getInputStream(fileUpload.getName());
                    ZipEntry anEntry = new ZipEntry("uploadedFiles" + File.separatorChar + fileUpload.getName());
                    zipOutputStream.putNextEntry(anEntry);
                    IOUtils.copy(inputStream, zipOutputStream);
                    IOUtils.closeQuietly(inputStream);
                    zipOutputStream.closeEntry();
                    zipOutputStream.flush();
                }catch (FileNotFoundException e) {
                    //ignore for development
                    if(!Boolean.getBoolean("hypersocket.development")) {
                        throw e;
                    }
                }
            }
        }
    }
}
