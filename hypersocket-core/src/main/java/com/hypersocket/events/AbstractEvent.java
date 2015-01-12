package com.hypersocket.events;

import org.springframework.context.ApplicationEvent;

public class AbstractEvent extends ApplicationEvent {

	private static final long serialVersionUID = -8962107614397284343L;

	public AbstractEvent(Object source) {
		super(source);
	}

	public String[] getResourceKeys() {
		return new String[] { };
	}
}
