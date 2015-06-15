package com.hypersocket.dashboard.message;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hypersocket.HypersocketVersion;
import com.hypersocket.http.HttpUtils;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.resource.ResourceException;

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

	private void checkMessages() throws IOException, AccessDeniedException, ResourceException {

		ObjectMapper mapper = new ObjectMapper();

		try {
			
			List<DashboardMessage> results = new ArrayList<DashboardMessage>();
			
			// Get global links
			results.addAll(Arrays.asList(mapper.readValue(HttpUtils.doHttpGet(
					MESSAGES_URL + "messages.json",
					true), DashboardMessage[].class)));
			
			// Get product links
			results.addAll(Arrays.asList(mapper.readValue(HttpUtils.doHttpGet(
					MESSAGES_URL + HypersocketVersion.getProductId() + "/messages.json",
					true), DashboardMessage[].class)));
			
			service.saveNewMessages(results.toArray(new DashboardMessage[0]));
		} catch (Throwable e) {
			throw new ResourceException(DashboardMessageServiceImpl.RESOURCE_BUNDLE,
					"error.readingMessageList", e.getMessage());
		}
	}
}
