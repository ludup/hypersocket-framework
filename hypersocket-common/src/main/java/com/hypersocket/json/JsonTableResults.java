package com.hypersocket.json;

public class JsonTableResults<T extends JsonResource> extends JsonResourceList<T> {

	long total;

	public long getTotal() {
		return total;
	}

	public void setTotal(long total) {
		this.total = total;
	}
	
	public void setRows(T[] resources) {
		this.resources = resources;
	}
	
}
