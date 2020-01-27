package com.hypersocket.password.history;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.Collection;

import javax.annotation.PostConstruct;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.StringUtils;
import org.bouncycastle.util.Arrays;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.hypersocket.config.ConfigurationService;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmAdapter;
import com.hypersocket.realm.RealmService;

@Service
public class PasswordHistoryServiceImpl implements PasswordHistroyService {

	@Autowired
	private ConfigurationService configurationService; 
	
	@Autowired
	private RealmService realmService; 
	
	@Autowired
	private PasswordHistoryRepository repository; 
	
	@PostConstruct
	private void postConstruct() {
		realmService.registerRealmListener(new RealmAdapter() {

			@Override
			public void onCreateRealm(Realm realm) {

				
				try {
					SecureRandom rnd = new SecureRandom();
					byte[] salt = new byte[32];
					rnd.nextBytes(salt);
					
					configurationService.setValue(realm, "passwordHistory.salt", 
							StringUtils.newStringUtf8(Base64.encodeBase64(salt, false)));
					configurationService.setValue(realm, "passwordHistory.configured", "true");
				} catch (Throwable t) {
					throw new IllegalStateException(t.getMessage(), t);
				}
			}

			@Override
			public boolean hasCreatedDefaultResources(Realm realm) {
				return configurationService.getBooleanValue(realm, "passwordHistory.configured");
			}
			
			
			@Override
			public void onDeleteRealm(Realm realm) {
				repository.deleteRealm(realm);
			}
		});
	
	}
	
	@Override
	public boolean checkPassword(Principal principal, String password, int previous) {
	
		try {
			byte[] salt = Base64.decodeBase64(configurationService.getValue(principal.getRealm(), "passwordHistory.salt"));
			byte[] passwordBytes = password.getBytes("UTF-8");
			
			byte[] encodedBytes = Arrays.concatenate(salt, passwordBytes);
			String encodedPassword = StringUtils.newStringUtf8(Base64.encodeBase64(encodedBytes, false));
			PasswordHistory pwd = repository.getHistoryFor(principal, encodedPassword);
			if(pwd==null) {
				// Never been used
				return true;
			}
			
			// It has been used. Check when
			Collection<PasswordHistory> history = repository.getPasswordHistory(principal, previous);
			
			if(history.contains(pwd)) {
				// In current history
				return false;
			}
			
			// Outside of current history
			return true;
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}
	
	@Override
	public void recordPassword(Principal principal, String password) {
		
		try {
			byte[] salt = Base64.decodeBase64(configurationService.getValue(principal.getRealm(), "passwordHistory.salt"));
			byte[] passwordBytes = password.getBytes("UTF-8");
			
			byte[] encodedBytes = Arrays.concatenate(salt, passwordBytes);
			
			PasswordHistory pwd = new PasswordHistory();
			pwd.setPrincipal(principal);
			pwd.setEncodedPassword(StringUtils.newStringUtf8(Base64.encodeBase64(encodedBytes, false)));
			
			repository.savePassword(pwd);
			
		} catch (UnsupportedEncodingException e) {
			throw new IllegalStateException(e.getMessage(), e);
		}
	}

}
