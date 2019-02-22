package com.hypersocket.automation;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import com.hypersocket.tasks.Task;
import com.hypersocket.triggers.TriggerResource;
import com.hypersocket.utils.HypersocketUtils;

@Entity
@Table(name="automations")
public class AutomationResource extends Task {

	private static final long serialVersionUID = 2363415972604814835L;

	@Column(name="resource_key")
	String resourceKey;
	
	@Column(name="task_starts")
	@Temporal(TemporalType.DATE)
	Date taskStarts;
	
	@Column(name="start_time")
	String startTime;
	
	@Column(name="task_ends")
	@Temporal(TemporalType.DATE)
	Date taskEnds;
	
	@Column(name="end_time")
	String endTime;
	
	@Column(name="repeat_type")
	AutomationRepeatType repeatType = AutomationRepeatType.NEVER;
	
	@Column(name="repeat_value")
	Integer repeatValue = 1;
	
	@Column(name="notes")
	@Lob
	String notes;
	
	@Column(name="transactional")
	Boolean transactional;
	
	@Column(name="automation_events")
	Boolean fireAutomationEvents;
	
	@OneToMany(fetch=FetchType.EAGER)
	@JoinTable(name="automations_trigger_resources")
	@Fetch(FetchMode.SELECT)
	Set<TriggerResource> triggers = new HashSet<TriggerResource>();
	
	public String getResourceKey() {
		return resourceKey;
	}
	
	public void setResourceKey(String resourceKey) {
		this.resourceKey = resourceKey;
	}
	
	public void setTaskStarts(Date taskStarts) {
		this.taskStarts = taskStarts;
	}
	
	public String getTaskStarts() {
		if(taskStarts == null) {
			return "";
		} else {
			return HypersocketUtils.formatDate(taskStarts, "yyyy-MM-dd");
		}
	}
	
	public void setTaskEnds(Date taskEnds) {
		this.taskEnds = taskEnds;
	}
	
	public String getTaskEnds() {
		if(taskEnds == null) {
			return "";
		} else {
			return HypersocketUtils.formatDate(taskEnds, "yyyy-MM-dd");
		}
	}
	
	public AutomationRepeatType getRepeatType() {
		return repeatType;
	}
	
	public void setRepeatType(AutomationRepeatType repeatType) {
		this.repeatType = repeatType;
	}
	
	public Integer getRepeatValue() {
		return repeatValue;
	}
	
	public void setRepeatValue(Integer repeatValue) {
		this.repeatValue = repeatValue;
	}
	
	public void setNotes(String notes) {
		this.notes = notes;
	}
	
	public String getNotes() {
		return notes;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}
	
	public Date getStartDate() {
		return taskStarts;
	}
	
	public Date getEndDate() {
		return taskEnds;
	}

	public Set<TriggerResource> getChildTriggers() {
		return triggers;
	}

	public void setChildTriggers(Set<TriggerResource> triggers) {
		this.triggers = triggers;
	}

	public Boolean getTransactional() {
		return transactional == null ? Boolean.FALSE : transactional;
	}

	public void setTransactional(Boolean transactional) {
		this.transactional = transactional;
	}

	public boolean isDailyJob() {
		return getStartDate()==null 
				&& getEndDate()==null 
				&& StringUtils.isNotEmpty(startTime)
				&& StringUtils.isNotEmpty(endTime) 
				&& !endTime.equals("0:00");
	}

	public Boolean getFireAutomationEvents() {
		return fireAutomationEvents == null ? Boolean.FALSE : fireAutomationEvents;
	}

	public void setFireAutomationEvents(Boolean fireAutomationEvents) {
		this.fireAutomationEvents = fireAutomationEvents;
	}
	
	
}
