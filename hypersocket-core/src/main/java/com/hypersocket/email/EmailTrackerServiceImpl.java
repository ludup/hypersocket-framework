package com.hypersocket.email;

import java.io.IOException;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.auth.AbstractAuthenticatedServiceImpl;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.email.events.EmailOpenedEvent;
import com.hypersocket.events.EventService;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.permissions.SystemPermission;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceNotFoundException;
import com.hypersocket.upload.FileUpload;
import com.hypersocket.upload.FileUploadService;
import com.hypersocket.utils.FileUtils;

@Service
public class EmailTrackerServiceImpl extends AbstractAuthenticatedServiceImpl implements EmailTrackerService {

	private final static Logger log = LoggerFactory.getLogger(EmailTrackerServiceImpl.class);
	
	@Autowired
	private EmailTrackerRepository repository;
	
	@Autowired
	private EventService eventService;
	
	@Autowired
	private RealmService realmService; 
	
	@Autowired
	private ConfigurationService configurationService; 
	
	@Autowired
	private SystemConfigurationService systemConfigurationService; 
	
	@Autowired
	private FileUploadService fileService;
	
	@PostConstruct
	private void postConstruct() {
		eventService.registerEvent(EmailOpenedEvent.class, EmailNotificationServiceImpl.RESOURCE_BUNDLE);
	}
	
	@Override
	public String generateNonTrackingUri(String uuid, Realm realm) throws AccessDeniedException {
		
		try(var c = tryWithElevatedPermissions(SystemPermission.SYSTEM)) {
			String externalHostname = realmService.getRealmHostname(realm);
			if(StringUtils.isBlank(externalHostname)) {
				externalHostname = configurationService.getValue(realm,"email.externalHostname");
			}
			if(StringUtils.isBlank(externalHostname)) {
				throw new AccessDeniedException("External hostname cannot be resolved for tracking image");
			}
			
			try {
				FileUpload file = fileService.getFileUpload(uuid);
				
				if(externalHostname.startsWith("http")) {
					return String.format("%s/%s/api/files/public/%s/%s", FileUtils.checkEndsWithNoSlash(externalHostname),
						systemConfigurationService.getValue("application.path"),
						uuid,
						file.getFileName());
				} else {
					return String.format("https://%s/%s/api/files/public/%s/%s", FileUtils.checkEndsWithNoSlash(externalHostname),
							systemConfigurationService.getValue("application.path"),
							uuid,
							file.getFileName());
				}
			} catch (ResourceNotFoundException  e) {
				return "#";
			}
		}
		catch(IOException ioe) {
			return "#";
		}
		
	}
	
	@Override
	public String generateTrackingUri(String uuid, String subject, String name, String emailAddress, Realm realm) throws AccessDeniedException{
		
		try(var c = tryWithElevatedPermissions(SystemPermission.SYSTEM)) {
			Principal principal = null;
			try {
				principal = realmService.getPrincipalByEmail(realm, emailAddress);
			} catch(ResourceNotFoundException ex) {
			}
			EmailReceipt receipt = repository.trackEmail(subject, emailAddress, realm, principal);
			FileUpload upload = fileService.getFileUpload(uuid);
			
			String externalHostname = realmService.getRealmHostname(realm);
			if(StringUtils.isBlank(externalHostname)) {
				externalHostname = configurationService.getValue(realm,"email.externalHostname");
			}
			if(StringUtils.isBlank(externalHostname)) {
				throw new AccessDeniedException("External hostname cannot be resolved for tracking image");
			}
			if(externalHostname.startsWith("http")) {
				return String.format("%s/%s/api/emails/receipt/%d/%s", 
						FileUtils.checkEndsWithNoSlash(externalHostname),
					systemConfigurationService.getValue("application.path"),
					receipt.getId(),
					upload.getFileName());
			} else {
				return String.format("https://%s/%s/api/emails/receipt/%d/%s", 
						FileUtils.checkEndsWithNoSlash(externalHostname),
						systemConfigurationService.getValue("application.path"),
						receipt.getId(),
						upload.getFileName());
			}
		} catch (IOException | ResourceNotFoundException e) {
			return "#";
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
