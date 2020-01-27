package com.hypersocket.email;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.hypersocket.resource.RealmResource;

@Entity
@Table(name="email_batch_items")
public class EmailBatchItem extends RealmResource {

	private static final long serialVersionUID = 7712547601212721547L;

	@Column(name="subject", length=1024)
	private String subject;
	
	@Column(name="text")
	@Lob
	private String text;

	@Column(name="html")
	@Lob
	private String html;
	
	@Column(name="reply_name")
	private String replyToName;
	
	@Column(name="reply_email")
	private String replyToEmail;
	
	@Column(name="to_name")
	private String toName;
	
	@Column(name="to_email")
	private String toEmail;
	
	@Column(name="track")
	private boolean track;
	
	@Column(name="archive")
	private Boolean archive = Boolean.FALSE;
	
	@Column(name="attachments")
	@Lob
	private String attachments;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date schedule;
	
	@Column(name="context")
	private String context;
	
	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public void setText(String text) {
		this.text = text;
	}

	public void setHtml(String html) {
		this.html = html;
	}

	public void setReplyToName(String replyToName) {
		this.replyToName = replyToName;
	}

	public void setReplyToEmail(String replyToEmail) {
		this.replyToEmail = replyToEmail;
	}

	public void setToName(String toName) {
		this.toName = toName;
	}

	public void setToEmail(String toEmail) {
		this.toEmail = toEmail;
	}

	public void setTrack(boolean track) {
		this.track = track;
	}

	public String getText() {
		return text;
	}

	public String getHtml() {
		return html;
	}

	public String getReplyToName() {
		return replyToName;
	}

	public String getReplyToEmail() {
		return replyToEmail;
	}

	public String getToName() {
		return toName;
	}

	public String getToEmail() {
		return toEmail;
	}

	public boolean getTrack() {
		return track;
	}

	public String getAttachments() {
		return attachments;
	}

	public void setAttachments(String attachments) {
		this.attachments = attachments;
	}

	public Date getSchedule() {
		return schedule;
	}

	public void setSchedule(Date schedule) {
		this.schedule = schedule;
	}

	public void setArchive(Boolean archive) {
		this.archive = archive;
	}
	
	public Boolean getArchive() {
		return archive == null ? Boolean.FALSE : archive;
	}
}
