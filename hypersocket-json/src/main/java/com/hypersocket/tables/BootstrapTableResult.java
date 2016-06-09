package com.hypersocket.tables;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class BootstrapTableResult<T> {

	long total;
	Collection<?> rows;
	T resource;
	
	public BootstrapTableResult() {
		
	}
	
	public BootstrapTableResult(Collection<?> rows, long total) {
		this.rows = rows;
		this.total = total;
	}

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}

	public Collection<?> getRows() {
		return rows;
	}

	public void setRows(Collection<?> rows) {
		this.rows = rows;
	}

	public T getResource() {
		return resource;
	}

	public void setResource(T resource) {
		this.resource = resource;
	}


}
