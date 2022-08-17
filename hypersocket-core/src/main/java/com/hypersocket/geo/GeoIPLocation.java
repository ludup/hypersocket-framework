package com.hypersocket.geo;

public interface GeoIPLocation {

	String getLatitude();

	String getLongitude();

	String getCountryCode();

	String getRegionCode();

	String getZip();

	String getRegionName();

	String getCountryName();

	String getType();

	String getCity();

	String getIp();

	String getHostname();
}
