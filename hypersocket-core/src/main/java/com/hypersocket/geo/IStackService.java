package com.hypersocket.geo;

import java.io.IOException;

import com.hypersocket.session.IStackLocation;

public interface IStackService {

	IStackLocation lookupGeoIP(String ipAddress) throws IOException;

}
