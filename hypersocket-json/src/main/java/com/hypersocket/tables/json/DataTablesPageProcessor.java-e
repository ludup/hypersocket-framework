package com.hypersocket.tables.json;

import java.util.List;

import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;

public interface DataTablesPageProcessor {

	Column getColumn(int col);
	
	List<?> getPage(String searchPattern, int start, int length, ColumnSort[] sorting) throws UnauthorizedException;
	
	Long getTotalCount(String searchPattern) throws UnauthorizedException;
}
