package com.hypersocket.telemetry;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.google.gson.JsonObject;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.ResourceException;

public interface TelemetryProducer {

	default void fill(JsonObject data) throws ResourceException, AccessDeniedException {
	}

	default void fillRealm(Realm realm, JsonObject data) throws ResourceException, AccessDeniedException {
	}
	
	public static String formatAsUTC(Date date) {
		if(date == null)
			return "";
		final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
		return sdf.format(date); 
	}
}
