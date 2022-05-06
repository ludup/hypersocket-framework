package com.hypersocket.telemetry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Locale;
import java.util.TimeZone;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonObject;

@Component
public class VMTelemetryProducer implements TelemetryProducer {

	@Autowired
	private TelemetryService telemetryService;

	public VMTelemetryProducer() {
	}

	@PostConstruct
	private void setup() {
		telemetryService.registerProducer(this);
	}

	@Override
	public void fill(JsonObject data) {
		data.addProperty("cores", Runtime.getRuntime().availableProcessors());
		data.addProperty("timeZoneName", TimeZone.getDefault().getDisplayName());
		data.addProperty("timeZone", TimeZone.getDefault().getID());
		data.addProperty("locale", Locale.getDefault().toLanguageTag());
		data.addProperty("os", System.getProperty("os.name"));
		data.addProperty("osVersion", System.getProperty("os.version"));
		data.addProperty("arch", System.getProperty("os.arch"));
		data.addProperty("memory", Runtime.getRuntime().totalMemory());
		data.addProperty("maxMemory", Runtime.getRuntime().maxMemory());
		File f = new File("/etc/debian_version");
		if(f.exists()) {
			try(BufferedReader r = new BufferedReader(new FileReader(f))) {
				data.addProperty("vmPlatformVersion", r.readLine());
			}
			catch(IOException ioe) {
			}
		}
	}

}
