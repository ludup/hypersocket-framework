package upgrade;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.local.LocalRealmProvider;
import com.hypersocket.local.LocalUser;
import com.hypersocket.local.LocalUserRepository;
import com.hypersocket.realm.Realm;
import com.hypersocket.realm.RealmService;
import com.hypersocket.tables.ColumnSort;

public class localRealm_2_DOT_2_DOT_0 implements Runnable {

	static Logger log = LoggerFactory.getLogger(localRealm_2_DOT_2_DOT_0.class);

	@Autowired
	RealmService realmService;

	@Autowired
	LocalUserRepository userRepository;

	@Override
	public void run() {

		log.info("Upgrading users to description field (from full name)");

		for (Realm realm : realmService.allRealms(LocalRealmProvider.class)) {

			log.info(String.format("Process users in realm %s", realm.getName()));

			for (Iterator<LocalUser> userIt = userRepository.iterateUsers(realm, new ColumnSort[0]); userIt
					.hasNext();) {
				LocalUser user = userIt.next();
				user.setDescription(user.getDescription());
				log.info(String.format("Updating properties for user %s", user.getPrincipalName()));
				userRepository.saveUser(user, null);
			}

		}

	}

}
