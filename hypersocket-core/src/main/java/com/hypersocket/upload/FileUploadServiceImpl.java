package com.hypersocket.upload;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.hypersocket.events.EventService;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.PermissionCategory;
import com.hypersocket.permissions.PermissionService;
import com.hypersocket.permissions.PermissionType;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.AbstractResourceRepository;
import com.hypersocket.resource.AbstractResourceServiceImpl;
import com.hypersocket.resource.ResourceChangeException;
import com.hypersocket.resource.ResourceCreationException;
import com.hypersocket.upload.events.FileUploadCreatedEvent;
import com.hypersocket.upload.events.FileUploadDeletedEvent;

@Service
public class FileUploadServiceImpl extends
		AbstractResourceServiceImpl<FileUpload> implements FileUploadService {

	public static final String RESOURCE_BUNDLE = "FileUploadService";
	public static final String UPLOAD_PATH = "conf/uploads/";

	@Autowired
	FileUploadRepository repository;

	@Autowired
	I18NService i18nService;

	@Autowired
	PermissionService permissionService;

	@Autowired
	EventService eventService;

	@PostConstruct
	private void postConstruct() {

		i18nService.registerBundle(RESOURCE_BUNDLE);

		PermissionCategory cat = permissionService.registerPermissionCategory(
				RESOURCE_BUNDLE, "category.fileUpload");

		for (FileUploadPermission p : FileUploadPermission.values()) {
			permissionService.registerPermission(p, cat);
		}

		eventService.registerEvent(FileUploadCreatedEvent.class,
				RESOURCE_BUNDLE, this);
		eventService.registerEvent(FileUploadDeletedEvent.class,
				RESOURCE_BUNDLE, this);

	}

	@Override
	public FileUpload createFile(MultipartFile file, Realm realm)
			throws ResourceCreationException, AccessDeniedException,
			IOException {

		assertPermission(FileUploadPermission.CREATE);

		String uuid = UUID.randomUUID().toString();
		FileUpload fileUpload = new FileUpload();
		fileUpload.setFileName(file.getOriginalFilename());
		fileUpload.setRealm(realm);
		fileUpload.setName(uuid);
		fileUpload.setFileSize(file.getSize());

		try {

			File f = new File("conf/uploads/" + realm.getId() + "/" + uuid);
			f.getParentFile().mkdirs();
			f.createNewFile();

			FileOutputStream fileOuputStream = null;
			try {
				fileOuputStream = new FileOutputStream(UPLOAD_PATH
						+ realm.getId() + "/" + uuid);
				fileOuputStream.write(file.getBytes());
				String md5 = DigestUtils.md5Hex(file.getBytes());
				fileUpload.setMd5Sum(md5);
			} finally {
				fileOuputStream.close();
			}

			createResource(fileUpload, new HashMap<String, String>());

			return fileUpload;
		} catch (Throwable e) {
			fireResourceCreationEvent(fileUpload, e);
			throw e;
		}

	}

	@Override
	public FileUpload getFileByUuid(String uuid) {
		return repository.getFileByUuid(uuid);
	}

	@Override
	public void deleteFile(FileUpload fileUpload)
			throws ResourceChangeException, AccessDeniedException {

		assertPermission(FileUploadPermission.DELETE);
		try {
			File file = new File(UPLOAD_PATH + fileUpload.getRealm().getId()
					+ "/" + fileUpload.getName());
			if (file.delete()) {
				deleteResource(fileUpload);

			} else {
				throw new ResourceChangeException(RESOURCE_BUNDLE,
						"error.noValidatorPresent");
			}
		} catch (Throwable e) {
			fireResourceDeletionEvent(fileUpload, e);
			throw e;
		}

	}

	@Override
	protected AbstractResourceRepository<FileUpload> getRepository() {
		return repository;
	}

	@Override
	protected String getResourceBundle() {
		return RESOURCE_BUNDLE;
	}

	@Override
	public Class<? extends PermissionType> getPermissionType() {
		return FileUploadPermission.class;
	}

	@Override
	protected void fireResourceCreationEvent(FileUpload resource) {
		eventService.publishEvent(new FileUploadCreatedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceCreationEvent(FileUpload resource, Throwable t) {
		eventService.publishEvent(new FileUploadCreatedEvent(this, resource, t,
				getCurrentSession()));
	}

	@Override
	protected void fireResourceUpdateEvent(FileUpload resource) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void fireResourceUpdateEvent(FileUpload resource, Throwable t) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void fireResourceDeletionEvent(FileUpload resource) {
		eventService.publishEvent(new FileUploadDeletedEvent(this,
				getCurrentSession(), resource));
	}

	@Override
	protected void fireResourceDeletionEvent(FileUpload resource, Throwable t) {
		eventService.publishEvent(new FileUploadDeletedEvent(this, resource, t,
				getCurrentSession()));
	}

}
