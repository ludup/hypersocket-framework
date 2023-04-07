package com.hypersocket.unauthorized;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import com.hypersocket.alert.AlertCallback;
import com.hypersocket.alert.AlertService;
import com.hypersocket.auth.AuthenticationAttemptEvent;
import com.hypersocket.auth.FakePrincipal;
import com.hypersocket.config.ConfigurationService;
import com.hypersocket.message.MessageResourceService;
import com.hypersocket.realm.LogonException;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.PrincipalSuspensionService;
import com.hypersocket.realm.PrincipalSuspensionType;
import com.hypersocket.realm.PrincipalType;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.realm.UserPrincipal;
import com.hypersocket.resource.ResourceException;

@Service
public class UnauthorizedAccessServiceImpl implements UnauthorizedAccessService {

	static Logger log = LoggerFactory.getLogger(UnauthorizedAccessService.class);
	
	public static final String RESOURCE_BUNDLE = "EnhancedSecurityService";
	
	@Autowired
	private AlertService alertService; 
	
	@Autowired
	private ConfigurationService configurationService; 
	
	@Autowired
	private PrincipalSuspensionService suspensionService; 
	
	@Autowired
	private RealmService realmService; 
	
	@Autowired
	private MessageResourceService messageService;
	
	static final String MESSAGE_ACCOUNT_SUSPENDED = "accountSuspended";
	
	@PostConstruct
	private void postConstruct() {
		
		messageService.registerI18nMessage( 
				RESOURCE_BUNDLE, 
				"accountSuspended", 
				AccountSuspensionResolver.getVariables());
	}
	
	@Override
	public boolean verifyPassword(Realm realm, String scheme, String principal, char[] password) throws LogonException, IOException {
		
		if(realmService.verifyPrincipal(principal, realm)) {
			
			Principal p = realmService.getPrincipalByName(realm, principal, PrincipalType.USER);
			if(Objects.nonNull(p)) {
				if(realmService.verifyPassword(p, password)) {
					return true;
				}
			}
		}
		
		processSuspension(realm, principal, scheme, "password");
		return false;
	}
	
	@EventListener
	@Override
	public void onAuthenticationFailure(final AuthenticationAttemptEvent event) {
		if(!event.isSuccess()) {
			
			processSuspension(event.getCurrentRealm(), 
					event.getAttribute(AuthenticationAttemptEvent.ATTR_PRINCIPAL_NAME),
					event.getAttribute(AuthenticationAttemptEvent.ATTR_SCHEME),
					event.getAttribute(AuthenticationAttemptEvent.ATTR_MODULE));

		}
	}
	
	private void processSuspension(Realm realm, String principalName, String scheme, String module) {
		if(StringUtils.isNotBlank(principalName)) {
			
			StringBuffer alertKey = new StringBuffer();
			alertKey.append(realm.getName());
			alertKey.append("|");
			alertKey.append(principalName);
			alertKey.append("|");
			alertKey.append(module);
			
			final int failedAttempts = configurationService.getIntValue(realm, "lock.threshold");
			final int lockoutTime = configurationService.getIntValue(realm, "lock.timeout");
			final int period = configurationService.getIntValue(realm, "lock.period");
			
			alertService.processAlert(
					scheme, 
						alertKey.toString(), 
						30, 
						failedAttempts,
						lockoutTime, new AlertCallback<Void>() {

						@Override
						public Void alert() {
							
							Principal principal = realmService.getPrincipalByName(realm, 
									principalName, PrincipalType.USER);
							if(Objects.isNull(principal)) {
								principal = new FakePrincipal(principalName);
							}
							
							try {
								suspensionService.createPrincipalSuspension(principal, principalName, realm, new Date(), 
										configurationService.getLongValue(realm, "lock.period"), 
										PrincipalSuspensionType.MANUAL);
								
								if(principal!=null) {
									messageService.sendMessage(MESSAGE_ACCOUNT_SUSPENDED, 
											principal.getRealm(), 
										   new AccountSuspensionResolver((UserPrincipal<?>)principal, failedAttempts, lockoutTime, period),
										   principal);
								}
							} catch (ResourceException e) {
								log.error("Failed to create suspension", e);
							}
							return null;
						}
				
			});
		}
	}
}
