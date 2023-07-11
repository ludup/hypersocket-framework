package com.hypersocket.messagedelivery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.email.MessageDeliveryController;
import com.hypersocket.realm.MediaType;

@Service
public class MessageDeliveryServiceImpl extends AbstractAuthenticatedServiceImpl implements MessageDeliveryService {

	private Map<String, MessageDeliveryProvider<?>> providers = new HashMap<>();
	private MessageDeliveryController controller;
	
	@Autowired
	private SystemConfigurationService systemConfigurationService;
	
	@PostConstruct
	private void postConstruct() {
	}

	public MessageDeliveryController getController() {
		return controller;
	}

	@Override
	public void setController(MessageDeliveryController controller) {
		this.controller = controller;
	}

	@Override
	public void registerProvider(MessageDeliveryProvider<?> provider) {
		if(providers.containsKey(provider.getResourceKey()))
			throw new IllegalArgumentException(String.format("Provider with key '%s' already registered.", provider));
		providers.put(provider.getResourceKey(), provider);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <B extends MessageDeliveryBuilder> MessageDeliveryProvider<B> getBestProvider(MediaType media, Class<B> builder) {
		for(var p : providers.values()) {
			if(p.getSupportedMedia().equals(media) && p.isEnabled()) {
				return (MessageDeliveryProvider<B>)p;
			}
		}
		throw new IllegalArgumentException(String.format("No %s providers were available to deliver a message.", media));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <P extends MessageDeliveryProvider<?>> P getProvider(String resourceKey) {
		var p = providers.get(resourceKey);
		if(p == null)
			throw new IllegalArgumentException(String.format("No such messaging provider '%s'. Has an extension been uninstalled?", resourceKey));
		return (P)p;
	}

	@Override
	public List<MessageDeliveryProvider<?>> getProviders() {
		return Collections.unmodifiableList(
				new ArrayList<>(providers.values().stream().filter(p -> p.isEnabled()).collect(Collectors.toList())));
	}

	@Override
	public String getDefaultForMediaType(MediaType mediaType) {
		return  systemConfigurationService.getValue(mediaType.defaultProviderKey());
	}

}
