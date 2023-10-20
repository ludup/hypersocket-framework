package com.hypersocket.json;

import org.springframework.http.ResponseEntity;

@SuppressWarnings("serial")
public class ResponseEntityException extends Exception {
	
	private final ResponseEntity<?> entity;

	public ResponseEntityException(ResponseEntity<?> entity) {
		super("Responding with entity");
		this.entity = entity;
	}

	public ResponseEntityException(ResponseEntity<?> entity, String message, Throwable cause, boolean enableSuppression,
			boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
		this.entity = entity;
	}

	public ResponseEntityException(ResponseEntity<?> entity, String message, Throwable cause) {
		super(message, cause);
		this.entity = entity;
	}

	public ResponseEntityException(ResponseEntity<?> entity, String message) {
		super(message);
		this.entity = entity;
	}

	public ResponseEntityException(ResponseEntity<?> entity, Throwable cause) {
		super(cause);
		this.entity = entity;
	}

	public ResponseEntity<?> getEntity() {
		return entity;
	}
}
