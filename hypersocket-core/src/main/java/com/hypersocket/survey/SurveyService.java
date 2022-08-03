package com.hypersocket.survey;

import java.io.IOException;
import java.util.Map;

import org.springframework.context.event.ContextStartedEvent;

import com.hypersocket.events.CoreStartedEvent;
import com.hypersocket.permissions.AccessDeniedException;

public interface SurveyService {
	
	interface LicenseStateProvider {
		String getState();
		
		String getSubmissionUrl();
		
		String getConfigurationUrl();
	}
	
	/**
	 * Set an implementation that provides the current license state.
	 * 
	 * @param provider license state provider
	 */
	void setLicenseStateProvider(LicenseStateProvider provider);
	
	/**
	 * Get if a survey is ready to be displayed.
	 * 
	 * @param resourceKey resource key of survey
	 * @return ready
	 */
	boolean isSurveyReady(String resourceKey);
	
	/**
	 * Signal that a particular survey is ready, and should be presented to the appropriate user
	 * as soon as possible (e.g on admin login).
	 * 
	 * @param resourceKey surver resource key
	 */
	void surveyReady(String resourceKey);
	
	/**
	 * Get the survey object given its resource key. If the survey is not registered, or it doesn't
	 * exist in remotely supplied configuration, then <code>null</code> will be returned.
	 * 
	 * @param resourceKey
	 * @return survey or <code>null</code>.
	 */
	Survey getSurvey(String resourceKey);
	
	/**
	 * Register interest in a survey. The survey must also be provided by the remote configuration for
	 * it to be a candidate to be displayed.
	 * 
	 * @param survey
	 * @return the survey object if it exists.
	 */
	Survey registerSurvey(String survey);

	/**
	 * Submit data collected from a survey. After this point it will be marked as completed until
	 * the serial number changes.
	 * 
	 * @param resourceKey resource key
	 * @param data data
	 * @throws IOException on error
	 * @throws AccessDeniedException 
	 */
	void submitSurvey(String resourceKey, Map<String, String[]> data) throws IOException, AccessDeniedException;

	/**
	 * Context started.
	 * 
	 * @param started event
	 */
	void contextStarted(CoreStartedEvent started);

	/**
	 * Recreate the needed jobs to that trigger the readyness of a survey. This will examine
	 * the configured triggers and create jobs accordingly. If surveys are completed, no longer
	 * available, not in license (or any other rules), they will be unscheduled.
	 */
	void schedule();

	/**
	 * Get the next survey to display. If this is <code>null</code>, there are no
	 * more surveys to do (yet).
	 * 
	 * @return next survey
	 * @throws AccessDeniedException 
	 */
	Survey getNextReady() throws AccessDeniedException;

	/**
	 * Reject the given survey. The next nag will be rescheduled.
	 * 
	 * @param resourceKey key of survey
	 * @return the survey rejected
	 * @throws AccessDeniedException 
	 */
	Survey reject(String resourceKey) throws AccessDeniedException;
}
