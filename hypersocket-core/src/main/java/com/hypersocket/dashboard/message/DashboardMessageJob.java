package com.hypersocket.dashboard.message;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.permissions.AccessDeniedException;

public class DashboardMessageJob implements Job {

	public static final String MESSAGES_URL = "http://updates.hypersocket.com/messages/";

	@Autowired
	DashboardMessageService service;

	static Logger log = LoggerFactory.getLogger(DashboardMessageJob.class);

	public void execute(JobExecutionContext context)
			throws JobExecutionException {
		try {
			checkMessages();
		} catch (Throwable e) {
			log.error("Could not initialize DashboardMessageJob job", e);
		}

	}

	private void checkMessages() throws IOException, AccessDeniedException {

		URL url = new URL(MESSAGES_URL + System.getProperty("hypersocket.id")
				+ ".json");
		BufferedReader reader = new BufferedReader(new InputStreamReader(
				url.openStream()));
		StringBuilder stringBuilder = new StringBuilder();
		String line = null;
		while ((line = reader.readLine()) != null) {
			stringBuilder.append(line);
		}
		String jsonStr = stringBuilder.toString();
		ObjectMapper mapper = new ObjectMapper();

		DashboardMessage[] dashboardMessageList = mapper.readValue(jsonStr,
				DashboardMessage[].class);

		service.saveNewMessages(dashboardMessageList);
	}
}
