package com.hypersocket.upload.events;

import com.hypersocket.session.Session;
import com.hypersocket.upload.FileUpload;

public class FileUploadCreatedEvent extends FileUploadEvent {

	private static final long serialVersionUID = -2163788704078694618L;

	public static final String EVENT_RESOURCE_KEY = "fileUpload.created";

	public FileUploadCreatedEvent(Object source, Session session,
			FileUpload resource) {
		super(source, EVENT_RESOURCE_KEY, session, resource);
	}

	public FileUploadCreatedEvent(Object source, FileUpload resource,
			Throwable e, Session session) {
		super(source, EVENT_RESOURCE_KEY, resource, e, session);
	}

}
