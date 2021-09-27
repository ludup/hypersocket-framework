package upgrade;

import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.local.LocalRealmProvider;
import com.hypersocket.local.LocalUser;
import com.hypersocket.local.LocalUserRepository;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.tables.ColumnSort;

public class localRealm_1_DOT_2_DOT_1 implements Runnable {

	static Logger log = LoggerFactory.getLogger(localRealm_1_DOT_2_DOT_1.class);
	
	@Autowired
	private RealmService realmService;
	
	@Autowired
	private LocalUserRepository userRepository;
	
	@Override
	public void run() {
	
		log.info("Upgrading users to new entity property store");
		
		realmService.setupSystemContext();
		try {
			for(Realm realm : realmService.allRealms(LocalRealmProvider.class)) {
			
				log.info(String.format("Process users in realm %s", realm.getName()));
	
				for(Iterator<LocalUser> userIt = userRepository.iterateUsers(realm, new ColumnSort[0]); userIt.hasNext(); ) {
					LocalUser user = userIt.next();
					boolean updated = false;
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
		finally {
			realmService.clearPrincipalContext();
		}

	}

}
