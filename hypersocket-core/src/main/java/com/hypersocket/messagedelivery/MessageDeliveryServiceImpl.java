package com.hypersocket.messagedelivery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.email.MessageDeliveryController;
import com.hypersocket.json.version.HypersocketVersion;
import com.hypersocket.realm.MediaType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;

@Service
public class MessageDeliveryServiceImpl extends AbstractAuthenticatedServiceImpl implements MessageDeliveryService, SenderContext {

	private final static Logger LOG = LoggerFactory.getLogger(MessageDeliveryServiceImpl.class);
	
	private Map<String, MessageDeliveryProvider<?>> providers = new HashMap<>();
	private MessageDeliveryController controller;
	private Optional<SenderContext> senderContext = Optional.empty();
	
	@Autowired
	private SystemConfigurationService systemConfigurationService;
	
	@Autowired
	private RealmService realmService;
	

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


	@Override
	public SenderContext getSenderContext() {
		return senderContext.orElse(this);
	}

	@Override
	public void setSenderContext(SenderContext senderContext) {
		if(this.senderContext.isPresent())
			LOG.warn("Cannot set sender context more than once. The current implementation is {0}. The request implementation is {1}", this.senderContext.get().getClass().getName(), senderContext.getClass().getName());
		this.senderContext = Optional.of(senderContext);
	}

	@Override
	public String getAccountName(Realm realm) {
		return realmService.getRealmProperty(realm, "registration.company");
	}

	@Override
	public String getAccountID(Realm realm) {
		return HypersocketVersion.getSerial();
	}

}
