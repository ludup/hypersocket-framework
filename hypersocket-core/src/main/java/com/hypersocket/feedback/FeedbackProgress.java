package com.hypersocket.feedback;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.hypersocket.util.SpringApplicationContextProvider;

public class FeedbackProgress implements Serializable{

	private static final long serialVersionUID = 8164835181440211990L;
	String uuid;
	transient FeedbackService feedbackService;
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
	
	public synchronized void update(String error, String bundle, String[] options, FeedbackStatus status, String resourceKey, String... args) {
		final Feedback f = new Feedback(feedback.size(), status, resourceKey, false, null, args);
		f.setBundle(bundle);
		f.setError(error);
		f.setOptions(options);
		feedback.addLast(f);
	}
	
	public synchronized void update(FeedbackStatus status, String resourceKey, String... args) {
		feedback.addLast(new Feedback(feedback.size(), status, resourceKey, false, null, args));
	}

	public synchronized void complete(String resourceKey, String... args) {
		if(!isConfirming())
			feedback.addLast(new Feedback(feedback.size(), FeedbackStatus.SUCCESS, resourceKey, true, null, args));
	}

	public synchronized void failed(String resourceKey, Throwable t, String... args) {
		feedback.addLast(new Feedback(feedback.size(), FeedbackStatus.ERROR, resourceKey, true, t, args));
	}
	
	public synchronized List<Feedback> getFeedback() {
		return new ArrayList<Feedback>(feedback);
	}
	
	private void writeObject(ObjectOutputStream stream) throws IOException {
        stream.writeObject(uuid);
        stream.writeObject(feedback);
    }

    @SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
        uuid = (String) stream.readObject();
        feedback = (LinkedList<Feedback>) stream.readObject();
        feedbackService = SpringApplicationContextProvider.getApplicationContext().getBean("feedbackServiceImpl",FeedbackService.class);
    }

	public boolean isConfirming() {
		for(Feedback f : feedback)
			if(FeedbackStatus.CONFIRM == f.getStatus())
				return true;
		return false;
	}
}
