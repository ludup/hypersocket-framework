package com.hypersocket.email;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.hypersocket.realm.Principal;
import com.hypersocket.resource.SimpleResource;

@Entity
@Table(name="email_receipts")
public class EmailReceipt extends SimpleResource {

	private static final long serialVersionUID = 903483817231146776L;

	@OneToOne
	EmailTracker tracker;
	
	@Column(name="email_address")
	String emailAddress;
	
	@ManyToOne
	Principal principal;
	
	@Temporal(TemporalType.TIMESTAMP)
	Date opened;

	public EmailTracker getTracker() {
		return tracker;
	}

	public void setTracker(EmailTracker tracker) {
		this.tracker = tracker;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public Principal getPrincipal() {
		return principal;
	}

	public void setPrincipal(Principal principal) {
		this.principal = principal;
	}

	public Date getOpened() {
		return opened;
	}

	public void setOpened(Date opened) {
		this.opened = opened;
	}
	
	public String getName() {
		return getId().toString();
	}
	
}
