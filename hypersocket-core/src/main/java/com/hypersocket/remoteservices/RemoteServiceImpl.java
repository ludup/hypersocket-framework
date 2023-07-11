package com.hypersocket.remoteservices;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RemoteServiceImpl implements RemoteService {

	private final static Logger LOG = LoggerFactory.getLogger(RemoteServiceImpl.class);

	private Optional<RemoteServiceIdentificationProvider> idProvider = Optional.empty();
	
	@Autowired
	private DefaultRemoteServiceIdentificationProvider defaultIdProvider;
	

	@Override
	public void setIdentificationProvider(RemoteServiceIdentificationProvider idProvider) {
		if(this.idProvider.isPresent())
			LOG.warn("Cannot set sender context more than once. The current implementation is {0}. The request implementation is {1}", this.idProvider.get().getClass().getName(), idProvider.getClass().getName());
		this.idProvider = Optional.of(idProvider);
	}

	@Override
	public RemoteServiceIdentificationProvider getIdentificationProvider() {
		return idProvider.orElse(defaultIdProvider);
	}
}

