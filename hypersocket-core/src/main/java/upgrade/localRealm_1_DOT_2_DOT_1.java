package upgrade;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.local.LocalRealmProvider;
import com.hypersocket.local.LocalUser;
import com.hypersocket.local.LocalUserRepository;
import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;

public class localRealm_1_DOT_2_DOT_1 implements Runnable {

	static Logger log = LoggerFactory.getLogger(localRealm_1_DOT_2_DOT_1.class);
	
	@Autowired
	RealmService realmService;
	
	@Autowired
	LocalUserRepository userRepository;
	
	@Override
	public void run() {
	
		log.info("Upgrading users to new entity property store");
		
		for(Realm realm : realmService.allRealms(LocalRealmProvider.class)) {
		
			log.info(String.format("Process users in realm %s", realm.getName()));
			
			for(Principal principal : userRepository.allUsers(realm)) {
				boolean updated = false;
				LocalUser user = (LocalUser) principal;
				if(StringUtils.isBlank(user.getEmail())) {
					String email = userRepository.getValue(user, "user.email");
					if(StringUtils.isNotBlank(email)) {
						user.setEmail(email);
						updated = true;
					}
				}
				if(StringUtils.isBlank(user.getMobile())) {
					String mobile = userRepository.getValue(user, "user.mobile");
					if(StringUtils.isNotBlank(mobile)) {
						user.setMobile(mobile);
						updated = true;
					}
				}
				if(StringUtils.isBlank(user.getFullname())) {
					String fullname = userRepository.getValue(user, "user.fullname");
					if(StringUtils.isNotBlank(fullname)) {
						user.setFullname(fullname);
						updated = true;
					}
				}
				if(updated) {
					log.info(String.format("Updating properties for user %s", user.getPrincipalName()));
					userRepository.saveUser(user, null);
					userRepository.deleteProperties(user, "user.email", "user.mobile", "user.fullname");
				}
			}
		
		}

	}

}
