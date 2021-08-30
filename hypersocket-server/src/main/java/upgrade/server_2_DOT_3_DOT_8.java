package upgrade;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.realm.RealmService;
import com.hypersocket.resource.ResourceException;
import com.hypersocket.server.interfaces.http.HTTPInterfaceResource;
import com.hypersocket.server.interfaces.http.HTTPInterfaceResourceRepository;
import com.hypersocket.upgrade.PermissionsAwareUpgradeScript;

public class server_2_DOT_3_DOT_8 extends PermissionsAwareUpgradeScript {

	static Logger log = LoggerFactory.getLogger(server_2_DOT_3_DOT_8.class);

	@Autowired
	private HTTPInterfaceResourceRepository repository;

	@Autowired
	private RealmService realmService;

	@Override
	protected void doUpgrade() {

		HTTPInterfaceResource http = repository.getResourceByName("Default HTTP", realmService.getSystemRealm());
		if (http == null) {
			log.warn("Could not find default HTTP interface, cannot make it system");
		} else {
			http.setSystem(true);
			try {
				repository.saveResource(http);
			} catch (ResourceException e) {
				throw new IllegalStateException(e);
			}
		}

		HTTPInterfaceResource https = repository.getResourceByName("Default HTTPS", realmService.getSystemRealm());
		if (https == null) {
			log.warn("Could not find default HTTPS interface, cannot make it system");
		} else {
			https.setSystem(true);
			try {
				repository.saveResource(http);
			} catch (ResourceException e) {
				throw new IllegalStateException(e);
			}
		}

	}

}
