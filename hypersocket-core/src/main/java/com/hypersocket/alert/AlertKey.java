package com.hypersocket.alert;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@Entity
@Table(name="alert_keys")
public class AlertKey {

	@Id
	@GeneratedValue(strategy=GenerationType.TABLE)
	@Column(name="alert_key_id")
	Long id;

	@Column(name="resourceKey")
	String resourceKey; 
	
	@Column(name="alert_key")
	String key;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="triggered")
	Date triggered;


	public Long getId() {
		return id;
	}
	
	public void setId(Long id) {
		this.id = id;
	}
	
	public String getResourceKey() {
		return resourceKey;
	}

	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Date getTriggered() {
		return triggered;
	}

	public void setTriggered(Date triggered) {
		this.triggered = triggered;
	}
	
	
}
