package com.hypersocket.dashboard;

import java.io.Serializable;

public class UsageStat implements Serializable {

	private static final long serialVersionUID = 5448905278831525452L;
	
	String label;
	Long value;
	
	public UsageStat(String label, long value) {
		this.label = label;
		this.value = value;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Long getValue() {
		return value;
	}

	public void setValue(Long value) {
		this.value = value;
	}
	
	public String getLegendText() {
		return getLabel();
	}
	

}
