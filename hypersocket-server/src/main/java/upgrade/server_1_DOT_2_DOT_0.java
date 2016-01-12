package upgrade;

import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.certificates.CertificateResourceRepository;
import com.hypersocket.certificates.CertificateResourceServiceImpl;
import com.hypersocket.config.SystemConfigurationService;
import com.hypersocket.realm.RealmService;
import com.hypersocket.server.interfaces.HTTPInterfaceResource;
import com.hypersocket.server.interfaces.HTTPInterfaceResourceRepository;
import com.hypersocket.server.interfaces.HTTPProtocol;

public class server_1_DOT_2_DOT_0 implements Runnable {

	static Logger log = LoggerFactory.getLogger(server_1_DOT_2_DOT_0.class);
	
	@Autowired
	HTTPInterfaceResourceRepository repository;
	
	@Autowired
	SystemConfigurationService configurationService;
	
	@Autowired
	CertificateResourceRepository certificateService; 
	
	@Autowired
	RealmService realmService;
	
	@Override
	public void run() {

			
		try {
			HTTPInterfaceResource http = new HTTPInterfaceResource();
			http.setInterfaces(configurationService.getValue("listening.interfaces"));
			http.setName("Default HTTP");
			http.setRealm(realmService.getSystemRealm());
			http.setProtocol(HTTPProtocol.HTTP.toString());
			http.setPort(configurationService.getIntValue("http.port"));
			http.setRedirectHTTPS(configurationService.getBooleanValue("require.https"));
			http.setRedirectPort(configurationService.getIntValue("https.port"));
			
			HTTPInterfaceResource https = new HTTPInterfaceResource();
			https.setInterfaces(configurationService.getValue("listening.interfaces"));
			https.setName("Default HTTPS");
			https.setRealm(realmService.getSystemRealm());
			https.setProtocol(HTTPProtocol.HTTPS.toString());
			https.setPort(configurationService.getIntValue("https.port"));
			https.setCertificate(certificateService.getResourceByName(CertificateResourceServiceImpl.DEFAULT_CERTIFICATE_NAME, 
					realmService.getSystemRealm()));
			
			repository.saveResource(http, new HashMap<String,String>());
			repository.saveResource(https, new HashMap<String,String>());
		} catch (Throwable e) {
			log.error("Error converting to new HTTP interface settings. Use emergency port access to re-configure your server.", e);
		}
		
	}

}
