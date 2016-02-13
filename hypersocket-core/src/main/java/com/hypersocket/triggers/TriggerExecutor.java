package com.hypersocket.triggers;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.events.SystemEvent;

public interface TriggerExecutor extends AuthenticatedService {

	void processEventTrigger(TriggerResource trigger, SystemEvent event) throws ValidationException;

	void scheduleOrExecuteTrigger(TriggerResource trigger, SystemEvent event) throws ValidationException;

}
