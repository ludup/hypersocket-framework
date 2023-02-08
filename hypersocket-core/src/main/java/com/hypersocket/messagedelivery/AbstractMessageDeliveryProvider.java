package com.hypersocket.messagedelivery;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.email.MessageDeliveryController;
import com.hypersocket.realm.MediaType;
import com.hypersocket.realm.Realm;

public abstract class AbstractMessageDeliveryProvider<B extends MessageDeliveryBuilder>
		implements MessageDeliveryProvider<B> {

	@Autowired
	protected MessageDeliveryService messageDeliveryService;
	
	private MessageDeliveryController controller;
	private final String resourceKey;
	private final MediaType supportedMedia;

	public AbstractMessageDeliveryProvider(String resourceKey, MediaType supportedMedia) {
		this.resourceKey = resourceKey;
		this.supportedMedia = supportedMedia;
	}

	@PostConstruct
	private void postConstruct() {
		messageDeliveryService.registerProvider(this);
	}
	
	protected MessageDeliveryService getMessageDeliveryService() {
		return messageDeliveryService;
	}

	@Override
	public final B newBuilder(Realm realm) {
		if (realm == null)
			throw new IllegalArgumentException("Realm must be provided.");
		var b = createBuilder();
		b.realm(realm);
		return b;
	}

	protected abstract B createBuilder();

	@Override
	public final MediaType getSupportedMedia() {
		return supportedMedia;
	}

	@Override
	public final String getResourceKey() {
		return resourceKey;
	}

	protected abstract MessageDeliveryResult doSend(B builder) throws MessageDeliveryException;

	public final MessageDeliveryController getController() {
		return controller;
	}

	public final void setController(MessageDeliveryController controller) {
		this.controller = controller;
	}
}
