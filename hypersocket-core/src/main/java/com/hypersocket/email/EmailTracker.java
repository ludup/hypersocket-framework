package com.hypersocket.email;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.hypersocket.realm.Principal;
import com.hypersocket.realm.Realm;
import com.hypersocket.resource.SimpleResource;

@Entity
@Table(name="email_tracking")
public class EmailTracker extends SimpleResource {

	private static final long serialVersionUID = -2245463597525844610L;

	@Column(name="subject")
	private String subject;
	
	@Column(name="email_address")
	private String emailAddress;
	
	@ManyToOne
	@JoinColumn(name = "principal_resource_id", foreignKey = @ForeignKey(name = "email_tracking_cascade_2"))
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Principal principal;

	@ManyToOne
	@JoinColumn(name = "realm_resource_id", foreignKey = @ForeignKey(name = "email_tracking_cascade_1"))
	@OnDelete(action = OnDeleteAction.CASCADE)
	private Realm realm;
	
	@Temporal(TemporalType.TIMESTAMP)
	private Date opened;
	
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getEmailAddress() {
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress) {
		this.emailAddress = emailAddress;
	}

	public Realm getRealm() {
		return realm;
	}

	public void setRealm(Realm realm) {
		this.realm = realm;
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
