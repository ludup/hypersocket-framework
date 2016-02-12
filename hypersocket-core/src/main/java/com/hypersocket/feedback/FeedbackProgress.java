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
	
	public synchronized void update(FeedbackStatus status, String resourceKey) {
		feedback.addLast(new Feedback(feedback.size(), status, resourceKey, false));
	}

	public synchronized void complete(String resourceKey) {
		feedback.addLast(new Feedback(feedback.size(), FeedbackStatus.SUCCESS, resourceKey, true));
	}

	public synchronized void failed(String resourceKey, Throwable t) {
		feedback.addLast(new Feedback(feedback.size(), FeedbackStatus.ERROR, resourceKey, true));
	}
	
	public synchronized List<Feedback> getFeedback() {
		return new ArrayList<Feedback>(feedback);
	}
}
