package com.hypersocket.scheduler;

import com.hypersocket.tables.Column;

public enum SchedulerResourceColumns implements Column {

	ID, GROUP, NAME, STATUS, NEXTFIRE, LASTFIRE;
	
	public String getColumnName() {
		switch(this) {
		case ID:
			return "id";
		case GROUP:
			return "group";
		case STATUS:
			return "status";
		case NEXTFIRE:
			return "nextFire";
		case LASTFIRE:
			return "lastFire";
		default:
			return "name";
		}
	}
}