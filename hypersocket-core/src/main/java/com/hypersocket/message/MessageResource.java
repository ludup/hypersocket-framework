package com.hypersocket.message;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import com.hypersocket.resource.RealmResource;

@Entity
@Table(name="message_resource", uniqueConstraints = @UniqueConstraint(columnNames = {"message_id", "realm_id"}))
public class MessageResource extends RealmResource {

	private static final long serialVersionUID = -5676595715637581705L;

	@Column(name="message_id")
	Integer messageId;
	
	@Column(name="subject", length=1024)
	String subject;
	
	@Column(name="body")
	@Lob
	String body;
	
	@Column(name="html")
	@Lob
	String html;
	
	@Column(name="enabled")
	Boolean enabled;
	
	@Column(name="track")
	Boolean track;
	
	@Column(name="attachments", length=1024)
	String attachments;
	
	@Column(name="additional", length=1024)
	String additionalTo;
	
	@Column(name="reply_name", length=1024)
	String replyToName;
	
	@Column(name="reply_email", length=1024)
	String replyToEmail;

	
	public Integer getMessageId() {
		return messageId;
	}

	public void setMessageId(Integer messageId) {
		this.messageId = messageId;
	}

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

	public String getAdditionalTo() {
		return additionalTo;
	}

	public void setAdditionalTo(String additionalTo) {
		this.additionalTo = additionalTo;
	}

	public String getReplyToName() {
		return replyToName;
	}

	public void setReplyToName(String replyToName) {
		this.replyToName = replyToName;
	}

	public String getReplyToEmail() {
		return replyToEmail;
	}

	public void setReplyToEmail(String replyToEmail) {
		this.replyToEmail = replyToEmail;
	}		
}
