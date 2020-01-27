package com.hypersocket.tables;


public class ColumnSort {

	private Column column;
	private Sort sort;
	
	public ColumnSort(Column col, Sort sort) {
		this.column = col;
		this.sort = sort;
	}

	public Column getColumn() {
		return column;
	}
	
	public Sort getSort() {
		return sort;
	}
	
	
}
