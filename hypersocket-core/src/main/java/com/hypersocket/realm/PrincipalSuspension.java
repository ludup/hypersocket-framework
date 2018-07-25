package com.hypersocket.realm;

import java.util.Calendar;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hypersocket.resource.Resource;

@Entity
@Table(name = "suspensions")
public class PrincipalSuspension extends Resource {

	private static final long serialVersionUID = 5328311844604003267L;

	@ManyToOne
	@JoinColumn(name = "realm_id")
	protected Realm realm;

	@OneToOne
	@JoinColumn(name = "principal_id")
	protected Principal principal;
	
	@Column(name = "start_time")
	Date startTime;

	@Column(name = "duration")
	Long duration;

	@Column(name="suspension_type")
	PrincipalSuspensionType suspensionType;
	
	public PrincipalSuspension() {

	}

	public PrincipalSuspension(Realm realm, Principal principal,
			Date startTime, Long duration) {
		this.realm = realm;
		this.principal = principal;
		this.startTime = startTime;
		this.duration = duration;
	}

	@JsonIgnore
	public Realm getRealm() {
		return realm;
	}

	public void setRealm(Realm realm) {
		this.realm = realm;
	}

	@JsonIgnore
	public Principal getPrincipal() {
		return principal;
	}

	public void setPrincipal(Principal principal) {
		this.principal = principal;
	}

	public Date getStartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Long getDuration() {
		return duration;
	}

	public void setDuration(Long duration) {
		this.duration = duration;
	}

	public boolean isActive() {
		Calendar c = Calendar.getInstance();
		c.setTime(startTime);
		
		Date now = new Date();
		
		if(duration > 0) {
			Calendar c2 = Calendar.getInstance();
			c2.setTime(startTime);
			c2.add(Calendar.MINUTE, duration.intValue());
			
			return now.after(c.getTime()) && now.before(c2.getTime());
		} else {
			return now.after(c.getTime());
		}
		
	}

	public PrincipalSuspensionType getSuspensionType() {
		return suspensionType==null ? PrincipalSuspensionType.MANUAL : suspensionType;
	}

	public void setSuspensionType(PrincipalSuspensionType suspensionType) {
		this.suspensionType = suspensionType;
	}
	
	
}
