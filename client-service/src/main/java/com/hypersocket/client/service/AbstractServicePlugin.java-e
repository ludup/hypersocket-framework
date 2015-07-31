package com.hypersocket.client.service;

import java.io.IOException;
import java.util.Iterator;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractServicePlugin implements ServicePlugin {

	static Logger log = LoggerFactory.getLogger(AbstractServicePlugin.class);
	
	protected int processResourceList(String json, ResourceMapper mapper, String resourceName)
			throws IOException {
		try {
			JSONParser parser = new JSONParser();

			JSONObject result = (JSONObject) parser.parse(json);

			if (log.isDebugEnabled()) {
				log.debug(result.toJSONString());
			}

			JSONArray fields = (JSONArray) result.get("resources");

			if(fields.size() == 0) {
				if(log.isInfoEnabled()){
					log.info("There are no " + resourceName + " to start");
				}
				return 0;
			}
			
			int totalResources = 0;
			int totalErrors = 0;

			@SuppressWarnings("unchecked")
			Iterator<JSONObject> it = (Iterator<JSONObject>) fields.iterator();
			while (it.hasNext()) {
				if (!mapper.processResource(it.next())) {
					totalErrors++;
				}
				totalResources++;
			}

			if (totalErrors == totalResources) {
				// We could not start any resources
				throw new IOException("No resources could be started!");
			}

			return totalErrors;

		} catch (ParseException e) {
			throw new IOException("Failed to parse network resources json", e);
		}
	}
}
