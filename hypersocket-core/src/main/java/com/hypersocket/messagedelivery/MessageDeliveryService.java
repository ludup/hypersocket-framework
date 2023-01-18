package com.hypersocket.messagedelivery;

import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import com.hypersocket.email.MessageDeliveryController;
import com.hypersocket.realm.MediaType;

public interface MessageDeliveryService  {
	
	void setSenderContext(SenderContext senderContext);

	MessageDeliveryController getController();

	void setController(MessageDeliveryController controller);

	void registerProvider(MessageDeliveryProvider<? extends MessageDeliveryBuilder> provider);
	
	String getDefaultForMediaType(MediaType mediaType);
	
	default <B extends MessageDeliveryBuilder> MessageDeliveryProvider<B> getProviderOrBest(MediaType media, String provider, Class<B> builder) {
		MessageDeliveryProvider<B> p = null;
		if(StringUtils.isBlank(provider)) {
			var def = getDefaultForMediaType(media);
			if(StringUtils.isNotBlank(def)) {
				p = getProvider(def);
				if(p != null && !p.isEnabled()) {
					throw new IllegalStateException(String.format("The default %s provider (%s) is not enabled. This may be due to configuration or licensing.", media, def));
				}
				if(!p.isDefault()) {
					throw new IllegalStateException(String.format("The %s provider (%s) cannot be used the default. Please select a different default provider, or select a specific provider for this operation.", media, def));
				}
				return p;
			}
			return getBestProvider(media, builder);
		}
		else {
			/* If a provider is specified, it MUST be available and enabled */
			p = getProvider(provider);
			if(p != null && !p.isEnabled()) {
				throw new IllegalStateException(String.format("Required %s provider (%s) is not enabled. This may be due to configuration or licensing.", media, provider));
			}
			return p;
		}
	}

	<B extends MessageDeliveryBuilder> MessageDeliveryProvider<B> getBestProvider(MediaType media, Class<B> builder);
	
	<P extends MessageDeliveryProvider<?>> P getProvider(String resourceKey);

	List<MessageDeliveryProvider<?>> getProviders();

	default List<MessageDeliveryProvider<?>> getProviders(MediaType type) {
		return getProviders().stream().filter(p -> p.getSupportedMedia().equals(type)).collect(Collectors.toList());
	}

	SenderContext getSenderContext();
}
