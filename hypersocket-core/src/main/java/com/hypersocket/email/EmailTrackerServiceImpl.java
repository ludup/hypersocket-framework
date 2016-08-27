package com.hypersocket.email;

import java.util.Date;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.config.ConfigurationService;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.events.EventService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.utils.FileUtils;

@Service
public class EmailTrackerServiceImpl implements EmailTrackerService {

	static Logger log = LoggerFactory.getLogger(EmailTrackerServiceImpl.class);
	
	@Autowired
	EmailTrackerRepository repository;
	
	@Autowired
	EventService eventService;
	
	@Autowired
	RealmService realmService; 
	
	@Autowired
	ConfigurationService configurationService; 
	
	@Autowired
	SystemConfigurationService systemConfigurationService; 
	
	@PostConstruct
	private void postConstruct() {
		
	}
	
	@Override
	public String generateNonTrackingUri(String uuid, Realm realm) throws AccessDeniedException {
		
		String externalHostname = realmService.getRealmHostname(realm);
		if(StringUtils.isBlank(externalHostname)) {
			externalHostname = configurationService.getValue(realm,"email.externalHostname");
		}
		if(StringUtils.isBlank(externalHostname)) {
			throw new AccessDeniedException("External hostname cannot be resolved for tracking image");
		}
		if(externalHostname.startsWith("http")) {
			return String.format("%s/%s/api/files/download/%s", FileUtils.checkEndsWithNoSlash(externalHostname),
				systemConfigurationService.getValue("application.path"),
				uuid);
		} else {
			return String.format("https://%s/%s/api/files/download/%s", FileUtils.checkEndsWithNoSlash(externalHostname),
					systemConfigurationService.getValue("application.path"),
					uuid);
		}
		
	}
	
	@Override
	public String generateTrackingUri(String subject, String name, String emailAddress, Realm realm) throws AccessDeniedException {
		
		Principal principal = null;
		try {
			principal = realmService.getPrincipalByEmail(realm, emailAddress);
		} catch(ResourceNotFoundException ex) {
		}
		EmailReceipt receipt = repository.trackEmail(subject, emailAddress, realm, principal);
		
		String externalHostname = realmService.getRealmHostname(realm);
		if(StringUtils.isBlank(externalHostname)) {
			externalHostname = configurationService.getValue(realm,"email.externalHostname");
		}
		if(StringUtils.isBlank(externalHostname)) {
			throw new AccessDeniedException("External hostname cannot be resolved for tracking image");
		}
		if(externalHostname.startsWith("http")) {
			return String.format("%s/%s/api/emails/receipt/%d/image.png", FileUtils.checkEndsWithNoSlash(externalHostname),
				systemConfigurationService.getValue("application.path"),
				receipt.getId());
		} else {
			return String.format("https://%s/%s/api/emails/receipt/%d/image.png", FileUtils.checkEndsWithNoSlash(externalHostname),
					systemConfigurationService.getValue("application.path"),
					receipt.getId());
		}
		
	}
	
	@Override
	public void finaliseReceipt(Long receiptId) {
		
		EmailReceipt receipt = repository.getReceiptById(receiptId);
		if(receipt!=null) {
			if(receipt.getOpened()==null) {
				receipt.setOpened(new Date());
				try {
					repository.saveReceipt(receipt);
					eventService.publishEvent(new EmailOpenedEvent(this, receipt, receipt.getTracker().getRealm()));
				} catch (Throwable e) {
					log.error(String.format("Failed to finalise email receipt %d", receiptId), e);
					eventService.publishEvent(new EmailOpenedEvent(this, e, receipt.getTracker().getRealm()));
				}
			}
		}
	}
}
