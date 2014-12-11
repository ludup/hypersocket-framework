package com.hypersocket.upload.events;

import com.hypersocket.realm.events.RealmResourceEvent;
import com.hypersocket.session.Session;
import com.hypersocket.upload.FileUpload;

public class FileUploadEvent extends RealmResourceEvent {

	public static final String ATTR_NAME = "attr.fileName";
	public static final String ATTR_MD5 = "attr.md5Sum";

	private static final long serialVersionUID = 1L;

	public FileUploadEvent(Object source, String resourceKey, Session session,
			FileUpload resource) {
		super(source, resourceKey, true, session, resource);
		addFileUploadAttribute(resource);
	}

	public FileUploadEvent(Object source, String resourceKey,
			FileUpload resource, Throwable e, Session session) {
		super(source, resourceKey, e, session, resource);
		addFileUploadAttribute(resource);
	}

	private void addFileUploadAttribute(FileUpload resource) {
		addAttribute(ATTR_NAME, resource.getFileName());
		addAttribute(ATTR_MD5, resource.getMd5Sum());
	}

}
