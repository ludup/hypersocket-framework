package com.hypersocket.realm;

import com.hypersocket.tables.Column;

public enum PrincipalColumns implements Column {

	PRINCIPAL_DESCRIPTION, PRINCIPAL_NAME;
	
	public String getColumnName() {
		switch(this.ordinal()) {
		case 0:
			return "principalDesc";
		default:
			return "name";
		
		}
	}
}
