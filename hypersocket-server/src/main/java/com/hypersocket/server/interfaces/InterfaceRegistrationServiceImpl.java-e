package com.hypersocket.server.interfaces;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Service;

@Service
public class InterfaceRegistrationServiceImpl implements InterfaceRegistrationService {

	Set<String> additionalInterfacesPages = new HashSet<String>();
	
	@Override
	public void registerAdditionalInterface(String page) {
		additionalInterfacesPages.add(page);
	}
	
	@Override
	public Set<String> getAdditionalInterfacePages() {
		return additionalInterfacesPages;
	}
	
}
