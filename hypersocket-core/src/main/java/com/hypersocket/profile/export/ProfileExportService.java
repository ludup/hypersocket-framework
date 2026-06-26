package com.hypersocket.profile.export;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;

import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;

public interface ProfileExportService {
	void registerExporter(ProfileExporter exporter);
	void unregisterExporter(ProfileExporter exporter);
	Set<ProfileExporter> allProfileExporters();
	void zipAllProfiles(OutputStream outputStream, Realm realm) throws IOException, AccessDeniedException;
}
