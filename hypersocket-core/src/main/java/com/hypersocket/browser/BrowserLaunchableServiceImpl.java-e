package com.hypersocket.browser;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.i18n.I18NService;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.RealmService;
import com.hypersocket.tables.ColumnSort;

@Service
public class BrowserLaunchableServiceImpl extends
		AbstractAuthenticatedServiceImpl implements BrowserLaunchableService {

	public static final String RESOURCE_BUNDLE = "BrowserLaunchable";
	
	private static SecureRandom random = new SecureRandom();
	private String fingerprint;

	@Autowired
	BrowserLaunchableRepository repository;

	@Autowired
	RealmService realmService;

	@Autowired
	I18NService i18nService;

	@PostConstruct
	private void postConstruct() {
		i18nService.registerBundle(RESOURCE_BUNDLE);
	}

	@Override
	public List<BrowserLaunchable> getPersonalResources(Principal principal) {
		return repository.getPersonalResources(realmService
				.getAssociatedPrincipals(principal));
	}

	@Override
	public List<BrowserLaunchable> searchPersonalResources(Principal principal,
			String search, int start, int length, ColumnSort[] sorting) {

		return repository.searchAssignedResources(
				realmService.getAssociatedPrincipals(principal), search, start,
				length, sorting);
	}

	@Override
	public long getPersonalResourceCount(Principal principal, String search) {

		return repository.getAssignedResourceCount(
				realmService.getAssociatedPrincipals(principal), search);
	}

	@Override
	public String getFingerprint() {
		return fingerprint;
	}

	@Override
	public void updateFingerprint() {
		fingerprint = new BigInteger(130, random).toString(32);
	}
}
