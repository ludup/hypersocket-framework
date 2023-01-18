package com.hypersocket.messagedelivery;

import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.email.EmailMessageDeliveryProvider;
import com.hypersocket.realm.MediaType;

public abstract class AbstractEMailMessageDeliveryProvider<B extends MessageDeliveryBuilder>
		extends AbstractMessageDeliveryProvider<B> implements EmailMessageDeliveryProvider<B> {

	@Autowired
	private SystemConfigurationService systemConfigurationService;

	public AbstractEMailMessageDeliveryProvider(String resourceKey) {
		super(resourceKey, MediaType.EMAIL);
	}

	@Override
	public final boolean isEnabled() {
		return !"false".equals(System.getProperty("hypersocket.mail", "true"))
				&& systemConfigurationService.getBooleanValue("email.on")
				&& (Objects.isNull(getMessageDeliveryService().getController())
						|| getMessageDeliveryService().getController().canSend(this));
	}

}
