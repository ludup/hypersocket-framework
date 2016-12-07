package com.hypersocket.upload;

import com.hypersocket.tables.Column;

public enum FileUploadColumn implements Column {

	NAME, FILESIZE, TYPE;

	public String getColumnName() {
		switch (this.ordinal()) {
		case 1:
			return "fileSize";
		case 2:
			return "type";
		default:
			return "fileName";
		}
	}
}