package com.hypersocket.tasks.email;

import com.hypersocket.email.events.EmailEvent;
import com.hypersocket.events.SystemEvent;
import com.hypersocket.realm.Realm;
import com.hypersocket.tasks.Task;
import com.hypersocket.tasks.TaskResult;

public class EmailTaskResult extends EmailEvent implements TaskResult {

	private static final long serialVersionUID = 712812739563857588L;

	public EmailTaskResult(Object source, Realm currentRealm, Task task, String subject, String body, String to) {
		super(source, currentRealm, task, subject, body, to);
	}

	public EmailTaskResult(Object source, Realm currentRealm, String subject, String body, String to) {
		super(source, currentRealm, subject, body, to);
	}

	public EmailTaskResult(Object source, Throwable e, Realm currentRealm, String subject, String body, String to) {
		super(source, e, currentRealm, subject, body, to);
	}

	@Override
	public boolean isPublishable() {
		return true;
	}

	@Override
	public SystemEvent getEvent() {
		return this;
	}

}
