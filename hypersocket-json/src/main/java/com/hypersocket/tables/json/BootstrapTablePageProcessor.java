package com.hypersocket.tables.json;

import java.util.Collection;

import com.hypersocket.auth.json.UnauthorizedException;
import com.hypersocket.permissions.AccessDeniedException;
import com.hypersocket.tables.Column;
import com.hypersocket.tables.ColumnSort;

public interface BootstrapTablePageProcessor {

	Column getColumn(String column);
	
	Collection<?> getPage(String searchColumn, String searchPattern, int start, int length, ColumnSort[] sorting) throws UnauthorizedException, AccessDeniedException;
	
	Long getTotalCount(String searchColumn, String searchPattern) throws UnauthorizedException, AccessDeniedException;

}
