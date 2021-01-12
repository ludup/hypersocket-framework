package com.hypersocket.geo;

import java.io.IOException;

public interface IStackService {

	IStackLocation lookupGeoIP(String ipAddress) throws IOException;

}
