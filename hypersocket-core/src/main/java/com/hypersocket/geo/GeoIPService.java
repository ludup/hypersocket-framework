package com.hypersocket.geo;

import java.io.IOException;

import com.hypersocket.realm.Realm;

public interface GeoIPService {
	
	boolean isConfigured(Realm realm);

	GeoIPLocation lookupGeoIP(String ipAddress) throws IOException;

}
