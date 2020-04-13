package com.hypersocket.scheduler;

import com.hypersocket.tables.Column;

public enum SchedulerResourceColumns implements Column {

	ID, GROUP, REALM, NAME, STATUS, NEXTFIRE, LASTFIRE, TIMETAKEN;
	
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
		case REALM:
			return "realm";
		case TIMETAKEN:
			return "timeTaken";
		default:
			return "name";
		}
	}
}