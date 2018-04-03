package com.hypersocket.triggers;

import java.util.List;

import com.hypersocket.auth.AuthenticatedService;
import com.hypersocket.events.SystemEvent;

public interface TriggerExecutor extends AuthenticatedService {

	void processEventTrigger(TriggerResource trigger, SystemEvent result, List<SystemEvent> sourceEvents) throws ValidationException;

	void scheduleOrExecuteTrigger(TriggerResource trigger, List<SystemEvent> sourceEvents) throws ValidationException;

}
