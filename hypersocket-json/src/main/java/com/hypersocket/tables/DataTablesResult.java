package com.hypersocket.tables;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class DataTablesResult {

	int sEcho = 1;
	String iTotalRecords;
	String iTotalDisplayRecords;
	Collection<?> aaData;
	
	public DataTablesResult() {
		
	}
	
	public DataTablesResult(Collection<?> aaData, long totalRecords, int sEcho) {
		this.aaData = aaData;
		this.sEcho = sEcho;
		iTotalRecords = String.valueOf(totalRecords);
		iTotalDisplayRecords = String.valueOf(totalRecords);
	}

	public int getsEcho() {
		return sEcho;
	}

	public void setsEcho(int sEcho) {
		this.sEcho = sEcho;
	}

	public String getiTotalRecords() {
		return iTotalRecords;
	}

	public void setiTotalRecords(String iTotalRecords) {
		this.iTotalRecords = iTotalRecords;
	}

	public String getiTotalDisplayRecords() {
		return iTotalDisplayRecords;
	}

	public void setiTotalDisplayRecords(String iTotalDisplayRecords) {
		this.iTotalDisplayRecords = iTotalDisplayRecords;
	}
	
	public Collection<?> getAaData() {
		return aaData;
	}

	public void setAaData(List<?> aaData) {
		this.aaData = aaData;
	}
	
}
