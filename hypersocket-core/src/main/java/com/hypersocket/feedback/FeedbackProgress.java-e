package com.hypersocket.feedback;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FeedbackProgress {

	String uuid;
	FeedbackService feedbackService;
	LinkedList<Feedback> feedback = new LinkedList<Feedback>();
	
	public FeedbackProgress() {		
	}
	
	synchronized void init(String uuid, FeedbackService feedbackService) {
		this.uuid = uuid;
		this.feedbackService = feedbackService;
	}
	
	public String getUuid() {
		return uuid;
	}
	
	public synchronized void update(FeedbackStatus status, String resourceKey, String... args) {
		feedback.addLast(new Feedback(feedback.size(), status, resourceKey, false, null, args));
	}

	public synchronized void complete(String resourceKey, String... args) {
		feedback.addLast(new Feedback(feedback.size(), FeedbackStatus.SUCCESS, resourceKey, true, null, args));
	}

	public synchronized void failed(String resourceKey, Throwable t, String... args) {
		feedback.addLast(new Feedback(feedback.size(), FeedbackStatus.ERROR, resourceKey, true, t, args));
	}
	
	public synchronized List<Feedback> getFeedback() {
		return new ArrayList<Feedback>(feedback);
	}
}
