package com.hypersocket.tasks.alert;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import com.hypersocket.tasks.Task;

@Entity
@Table(name="alert_key")
public class AlertKey {

	@Id
	@GeneratedValue(strategy = GenerationType.TABLE)
	@Column(name="alert_key_id")
	Long id;

	@OneToOne
	Task task;
	
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
	
	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
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
