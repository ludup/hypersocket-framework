package com.hypersocket.upload.events;

import com.hypersocket.session.Session;
import com.hypersocket.upload.FileUpload;

public class FileUploadDeletedEvent extends FileUploadEvent {

	private static final long serialVersionUID = 1274404744570794473L;

	public static final String EVENT_RESOURCE_KEY = "fileUpload.deleted";

	public FileUploadDeletedEvent(Object source, Session session,
			FileUpload resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public FileUploadDeletedEvent(Object source, FileUpload resource,
			Throwable e, Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

}
