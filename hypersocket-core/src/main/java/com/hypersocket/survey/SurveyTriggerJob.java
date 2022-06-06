package com.hypersocket.survey;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;

import com.hypersocket.scheduler.PermissionsAwareJob;

public class SurveyTriggerJob extends PermissionsAwareJob {

	public static final String SURVEY_RESOURCE_KEY = "surveyResourceKey";

	public SurveyTriggerJob() {
	}

	@Autowired
	private SurveyService surveyService;

	@Override
	protected void executeJob(JobExecutionContext context) throws JobExecutionException {
		surveyService.surveyReady(context.getTrigger().getJobDataMap().getString(SURVEY_RESOURCE_KEY));
	}

}
