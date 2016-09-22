package com.hypersocket.message;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.hypersocket.resource.RealmResource;

@Entity
@Table(name="message_resource")
public class MessageResource extends RealmResource {

	@Column(name="subject")
	String subject;
	
	@Column(name="body")
	String body;
	
	@Column(name="html")
	String html;
	
	@Column(name="enabled")
	Boolean enabled;
	
	@Column(name="track")
	Boolean track;
	
	@Column(name="attachments", length=1024)
	String attachments;
	
	@Column(name="event")
	String event;

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getHtml() {
		return html;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public Boolean getTrack() {
		return track;
	}

	public void setTrack(Boolean track) {
		this.track = track;
	}

	public String getAttachments() {
		return attachments;
	}

	public void setAttachments(String attachments) {
		this.attachments = attachments;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}
	
	
}
